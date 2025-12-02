package com.example.bandmaster.service;

import com.example.bandmaster.DTO.summary.CommissionDetailDTO;
import com.example.bandmaster.models.*;
import com.example.bandmaster.models.ReceivableHistory;
import com.example.bandmaster.models.ReceivableInstallment;
import com.example.bandmaster.repository.*;
import com.example.bandmaster.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommissionProcessingService {

    private final ProductCommissionRepository productCommissionRepository;
    private final CommissionInstallmentRepository commissionInstallmentRepository;
    private final CommissionStatusRepository statusRepository;
    private final ReceivableInstallmentRepository receivableInstallmentRepository;
    private final ReceivableHistoryRepository receivableHistoryRepository;
    private final DateUtils dateUtils;

    /**
     * Passo 1: Gera as parcelas de comissão para vendas novas
     */
    @Transactional
    public void generateCommissionInstallments() {
        log.info("Iniciando geração de parcelas de comissão...");

        CommissionStatus statusBlocked = statusRepository.findByName("BLOQUEADA")
                .orElseThrow(() -> new RuntimeException("Status BLOQUEADA não encontrado"));
        CommissionStatus statusWaiting = statusRepository.findByName("AGUARDANDO PAGAMENTO")
                .orElseThrow(() -> new RuntimeException("Status AGUARDANDO PAGAMENTO não encontrado"));

        List<ProductCommission> unprocessed = productCommissionRepository.findByProcessedFalse();

        for (ProductCommission prodComm : unprocessed) {
            try {
                processSingleProductCommission(prodComm, statusBlocked, statusWaiting);
            } catch (Exception e) {
                log.error("Erro ao processar comissão id: {}", prodComm.getId(), e);
            }
        }
        log.info("Geração de parcelas finalizada.");
    }

    private void processSingleProductCommission(ProductCommission prodComm, CommissionStatus blocked, CommissionStatus waiting) {
        BigDecimal totalCommission = prodComm.getRevenue()
                .multiply(prodComm.getCommissionPercentage())
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        List<ReceivableInstallment> parcels = receivableInstallmentRepository.findValidParcelsBySaleId(prodComm.getSaleId());

        if (parcels.isEmpty()) return;

        BigDecimal totalParcelsValue = parcels.stream()
                .map(ReceivableInstallment::getTitleValue) // Corrigido: Usa Valor do Título para rateio
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalParcelsValue.compareTo(BigDecimal.ZERO) == 0) return;

        for (ReceivableInstallment parcel : parcels) {
            BigDecimal proportion = parcel.getTitleValue()
                    .divide(totalParcelsValue, 10, RoundingMode.HALF_UP);

            BigDecimal commissionValue = totalCommission
                    .multiply(proportion)
                    .setScale(4, RoundingMode.HALF_UP);

            BigDecimal equivalentRevenue = prodComm.getRevenue()
                    .multiply(proportion)
                    .setScale(4, RoundingMode.HALF_UP);

            String term = parcel.getPaymentMethod() != null ? parcel.getPaymentMethod().getTerm() : "";
            CommissionStatus initialStatus = (term.equalsIgnoreCase("P") || term.equalsIgnoreCase("H"))
                    ? waiting : blocked;

            CommissionInstallment installment = new CommissionInstallment();
            installment.setProductCommission(prodComm);
            installment.setInstallment(parcel); // Passa o objeto Parcel completo
            installment.setTotalCommissionValue(commissionValue);
            installment.setEquivalentRevenue(equivalentRevenue);
            installment.setStatus(initialStatus);

            commissionInstallmentRepository.save(installment);
        }

        prodComm.setProcessed(true);
        productCommissionRepository.save(prodComm);
    }

    /**
     * Passo 2: Atualiza o status (Bloqueada -> Liberada/Paga) baseado no financeiro
     */
    @Transactional
    public void updateCommissionStatuses() {
        log.info("Iniciando atualização de status das comissões...");

        List<CommissionInstallment> installments = commissionInstallmentRepository.findAll();
        LocalDate today = LocalDate.now();

        // Carrega status para memória
        CommissionStatus stPaid = statusRepository.findByName("PAGA").orElseThrow();
        CommissionStatus stReversed = statusRepository.findByName("ESTORNADA").orElseThrow();
        CommissionStatus stWaiting = statusRepository.findByName("AGUARDANDO PAGAMENTO").orElseThrow();
        CommissionStatus stFree = statusRepository.findByName("LIBERADA").orElseThrow();
        CommissionStatus stBlocked = statusRepository.findByName("BLOQUEADA").orElseThrow();

        for (CommissionInstallment installment : installments) {
            // Se não tem parcela financeira, pula
            if (installment.getInstallment() == null) continue;

            // Busca histórico financeiro
            List<ReceivableHistory> histories = receivableHistoryRepository.findByInstallmentId(installment.getInstallment().getId());

            BigDecimal receivedValue = BigDecimal.ZERO;
            BigDecimal reversedValue = BigDecimal.ZERO;
            LocalDate lastPaymentDate = null;

            for (ReceivableHistory h : histories) {
                if ("E".equals(h.getStatus())) {
                    reversedValue = reversedValue.add(h.getValue());
                } else {
                    receivedValue = receivedValue.add(h.getValue());
                    if (lastPaymentDate == null || (h.getPaymentDate() != null && h.getPaymentDate().isAfter(lastPaymentDate))) {
                        lastPaymentDate = h.getPaymentDate();
                    }
                }
            }

            BigDecimal liquidValue = receivedValue.subtract(reversedValue);

            // Soma o que já foi pago de comissão (se houver pagamentos parciais)
            BigDecimal commissionPaid = BigDecimal.ZERO;
            if (installment.getPayments() != null) {
                commissionPaid = installment.getPayments().stream()
                        .map(CommissionPayment::getPaidValue)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }

            // Lógica de decisão de Status
            CommissionStatus newStatus;

            if (commissionPaid.compareTo(installment.getTotalCommissionValue()) >= 0) {
                newStatus = stPaid;
            } else if (reversedValue.compareTo(BigDecimal.ZERO) > 0
                    && commissionPaid.compareTo(liquidValue) > 0) {
                newStatus = stReversed;
            } else if (liquidValue.compareTo(BigDecimal.ZERO) <= 0) {
                newStatus = stWaiting;
            } else {
                // Lógica da data limite (5º dia útil)
                LocalDate limitDate = dateUtils.getFifthBusinessDayOfNextMonth(lastPaymentDate);

                if (limitDate != null && !today.isBefore(limitDate)) {
                    newStatus = stFree; // LIBERADA
                } else {
                    newStatus = stBlocked;
                }
            }

            // Só atualiza no banco se mudou o status
            if (installment.getStatus() == null || !installment.getStatus().getId().equals(newStatus.getId())) {
                installment.setStatus(newStatus);
                commissionInstallmentRepository.save(installment);
            }
        }
        log.info("Atualização de status finalizada.");
    }

    /**
     * Passo 3: Gera o relatório detalhado para API (JSON)
     */
    @Transactional(readOnly = true)
    public Map<String, List<CommissionDetailDTO>> getDetailedCommissionsMap() {
        Map<String, List<CommissionDetailDTO>> result = new java.util.HashMap<>();
        result.put("AGUARDANDO PAGAMENTO", new ArrayList<>());
        result.put("BLOQUEADA", new ArrayList<>());
        result.put("PAGA", new ArrayList<>());
        result.put("ESTORNADA", new ArrayList<>());
        result.put("LIBERADA", new ArrayList<>());

        List<CommissionInstallment> allInstallments = commissionInstallmentRepository.findAllWithDetails();

        for (CommissionInstallment c : allInstallments) {
            if (c.getInstallment() == null) continue;

            List<ReceivableHistory> histories = receivableHistoryRepository.findByInstallmentId(c.getInstallment().getId());
            BigDecimal valorRecebido = BigDecimal.ZERO;
            BigDecimal valorEstornado = BigDecimal.ZERO;
            LocalDate lastPaymentDate = null;

            for (ReceivableHistory h : histories) {
                if ("E".equals(h.getStatus())) {
                    valorEstornado = valorEstornado.add(h.getValue());
                } else {
                    valorRecebido = valorRecebido.add(h.getValue());
                    if (lastPaymentDate == null || (h.getPaymentDate() != null && h.getPaymentDate().isAfter(lastPaymentDate))) {
                        lastPaymentDate = h.getPaymentDate();
                    }
                }
            }

            LocalDate dataLimite = null;
            if (valorRecebido.compareTo(BigDecimal.ZERO) > 0) {
                dataLimite = dateUtils.getFifthBusinessDayOfNextMonth(lastPaymentDate);
            }

            BigDecimal comissaoPaga = BigDecimal.ZERO;
            if (c.getPayments() != null) {
                comissaoPaga = c.getPayments().stream()
                        .map(CommissionPayment::getPaidValue)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }

            // Montagem Segura do DTO
            boolean hasProductComm = c.getProductCommission() != null;
            boolean hasEmployee = hasProductComm && c.getProductCommission().getEmployee() != null;
            boolean hasProduct = hasProductComm && c.getProductCommission().getProduct() != null;

            Long vendedorId = hasEmployee ? c.getProductCommission().getEmployee().getId() : 0L;
            String vendedorNome = hasEmployee ? c.getProductCommission().getEmployee().getName() : "Vendedor N/D";
            Long produtoId = hasProduct ? c.getProductCommission().getProduct().getId() : 0L;
            String produtoDesc = hasProduct ? c.getProductCommission().getProduct().getDescription() : "Produto Removido";
            Long vendaId = hasProductComm ? c.getProductCommission().getSaleId() : 0L;
            LocalDateTime dataVenda = hasProductComm ? c.getProductCommission().getCreatedAt() : null;

            CommissionDetailDTO dto = new CommissionDetailDTO(
                    vendedorId,
                    vendedorNome,
                    c.getId(),
                    produtoId,
                    produtoDesc,
                    vendaId,
                    dataVenda,
                    c.getInstallment().getId(),
                    c.getEquivalentRevenue(),
                    valorRecebido,
                    valorEstornado,
                    c.getTotalCommissionValue(),
                    comissaoPaga,
                    (dataLimite != null) ? dataLimite.toString() : null
            );

            String statusName = (c.getStatus() != null) ? c.getStatus().getName() : "BLOQUEADA";
            result.computeIfAbsent(statusName, k -> new ArrayList<>()).add(dto);
        }

        return result;
    }
}