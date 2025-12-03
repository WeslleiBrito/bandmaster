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
    private final SalespersonReversalDeductionRepository deductionRepository;
    private final DateUtils dateUtils;

    // ============================================================================================
    // PASSO 1: GERAÇÃO (INSERTS)
    // Cria os registros físicos no banco de dados.
    // ============================================================================================

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
        log.info("Geração finalizada.");
    }

    private void processSingleProductCommission(ProductCommission prodComm, CommissionStatus statusBlocked, CommissionStatus statusWaiting) {
        BigDecimal totalCommission = prodComm.getRevenue()
                .multiply(prodComm.getCommissionPercentage())
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        List<ReceivableInstallment> parcels = receivableInstallmentRepository.findValidParcelsBySaleId(prodComm.getSaleId());
        if (parcels.isEmpty()) return;

        BigDecimal totalParcelsValue = parcels.stream()
                .map(ReceivableInstallment::getTitleValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalParcelsValue.compareTo(BigDecimal.ZERO) == 0) return;

        for (ReceivableInstallment parcel : parcels) {
            BigDecimal proportion = parcel.getTitleValue().divide(totalParcelsValue, 10, RoundingMode.HALF_UP);
            BigDecimal commissionValue = totalCommission.multiply(proportion).setScale(4, RoundingMode.HALF_UP);
            BigDecimal equivalentRevenue = prodComm.getRevenue().multiply(proportion).setScale(4, RoundingMode.HALF_UP);

            // REGRA: Se Prazo 'P' -> Aguardando. Outros (Vista) -> Bloqueada.
            String term = parcel.getPaymentMethod() != null ? parcel.getPaymentMethod().getTerm() : "";
            CommissionStatus initialStatus = "P".equalsIgnoreCase(term) ? statusWaiting : statusBlocked;

            CommissionInstallment installment = new CommissionInstallment();
            installment.setProductCommission(prodComm);
            installment.setInstallment(parcel);
            installment.setTotalCommissionValue(commissionValue);
            installment.setEquivalentRevenue(equivalentRevenue);
            installment.setStatus(initialStatus);

            commissionInstallmentRepository.save(installment);
        }
        prodComm.setProcessed(true);
        productCommissionRepository.save(prodComm);
    }

    // ============================================================================================
    // PASSO 2: RELATÓRIO VIRTUAL (READ-ONLY)
    // Calcula status e splits em tempo real para o JSON.
    // ============================================================================================

    @Transactional(readOnly = true)
    public Map<String, List<CommissionDetailDTO>> getDetailedCommissionsMap() {
        Map<String, List<CommissionDetailDTO>> result = new java.util.HashMap<>();
        result.put("AGUARDANDO PAGAMENTO", new ArrayList<>());
        result.put("BLOQUEADA", new ArrayList<>());
        result.put("LIBERADA", new ArrayList<>());
        result.put("PAGA", new ArrayList<>());
        result.put("ESTORNADA", new ArrayList<>());

        List<CommissionInstallment> allInstallments = commissionInstallmentRepository.findAllWithDetails();
        LocalDate today = LocalDate.now();

        for (CommissionInstallment c : allInstallments) {
            if (c.getInstallment() == null) continue;

            // 1. Coleta dados financeiros
            List<ReceivableHistory> histories = receivableHistoryRepository.findByInstallmentId(c.getInstallment().getId());
            BigDecimal valorRecebidoCliente = BigDecimal.ZERO;
            BigDecimal valorEstornadoCliente = BigDecimal.ZERO;
            LocalDate dataUltimoPagamento = null;

            for (ReceivableHistory h : histories) {
                if ("E".equals(h.getStatus())) {
                    valorEstornadoCliente = valorEstornadoCliente.add(h.getValue());
                } else {
                    valorRecebidoCliente = valorRecebidoCliente.add(h.getValue());
                    if (dataUltimoPagamento == null || (h.getPaymentDate() != null && h.getPaymentDate().isAfter(dataUltimoPagamento))) {
                        dataUltimoPagamento = h.getPaymentDate();
                    }
                }
            }

            BigDecimal valorLiquidoCliente = valorRecebidoCliente.subtract(valorEstornadoCliente);
            if (valorLiquidoCliente.compareTo(BigDecimal.ZERO) < 0) valorLiquidoCliente = BigDecimal.ZERO;

            // 2. Coleta dados do Vendedor
            BigDecimal jaPagoAoVendedor = c.getPayments() != null ?
                    c.getPayments().stream().map(CommissionPayment::getPaidValue).reduce(BigDecimal.ZERO, BigDecimal::add)
                    : BigDecimal.ZERO;

            List<SalespersonReversalDeduction> abatimentos = deductionRepository.findByCommissionInstallmentId(c.getId());
            BigDecimal jaAbatidoPeloVendedor = abatimentos.stream()
                    .map(SalespersonReversalDeduction::getDeductedValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal saldoDevedorVendedor = jaPagoAoVendedor.subtract(jaAbatidoPeloVendedor);
            if (saldoDevedorVendedor.compareTo(BigDecimal.ZERO) < 0) saldoDevedorVendedor = BigDecimal.ZERO;

            // 3. Aplicação das Regras
            BigDecimal valorTitulo = c.getInstallment().getTitleValue();
            BigDecimal totalComissao = c.getTotalCommissionValue();

            if (valorTitulo.compareTo(BigDecimal.ZERO) == 0) continue;

            // REGRA 5: ESTORNADA (Cliente devolveu e vendedor deve)
            if (valorEstornadoCliente.compareTo(BigDecimal.ZERO) > 0 && saldoDevedorVendedor.compareTo(BigDecimal.ZERO) > 0) {
                CommissionDetailDTO dto = createDTO(c, saldoDevedorVendedor, null);
                result.get("ESTORNADA").add(dto);
                continue; // Interrompe para este item
            }

            // Cálculo Proporcional (Split)
            BigDecimal percentualPago = valorLiquidoCliente.divide(valorTitulo, 4, RoundingMode.HALF_UP);
            if (percentualPago.compareTo(BigDecimal.ONE) > 0) percentualPago = BigDecimal.ONE;

            BigDecimal comissaoVirtualRecebida = totalComissao.multiply(percentualPago).setScale(2, RoundingMode.HALF_UP);
            BigDecimal comissaoVirtualPendente = totalComissao.subtract(comissaoVirtualRecebida).setScale(2, RoundingMode.HALF_UP);

            // Parte Recebida (Regras 1, 3, 4)
            if (comissaoVirtualRecebida.compareTo(BigDecimal.ZERO) > 0) {
                String statusDestino;

                if (jaPagoAoVendedor.compareTo(comissaoVirtualRecebida) >= 0) {
                    statusDestino = "PAGA"; // Regra 4
                } else {
                    LocalDate dataLimite = null;
                    if (dataUltimoPagamento != null) {
                        dataLimite = dateUtils.getFifthBusinessDayOfNextMonth(dataUltimoPagamento);
                    }

                    if (dataLimite != null && !today.isBefore(dataLimite)) {
                        statusDestino = "LIBERADA"; // Regra 3
                    } else {
                        statusDestino = "BLOQUEADA"; // Regra 1
                    }
                }

                CommissionDetailDTO dtoParteA = createDTO(c, comissaoVirtualRecebida,
                        (statusDestino.equals("LIBERADA") || statusDestino.equals("BLOQUEADA")) && dataUltimoPagamento != null
                                ? dateUtils.getFifthBusinessDayOfNextMonth(dataUltimoPagamento).toString()
                                : null);
                result.get(statusDestino).add(dtoParteA);
            }

            // Parte Pendente (Regra 2)
            if (comissaoVirtualPendente.compareTo(BigDecimal.ZERO) > 0) {
                CommissionDetailDTO dtoParteB = createDTO(c, comissaoVirtualPendente, null);
                result.get("AGUARDANDO PAGAMENTO").add(dtoParteB);
            }
        }

        return result;
    }

    private CommissionDetailDTO createDTO(CommissionInstallment c, BigDecimal valorComissaoVirtual, String dataLimite) {
        boolean hasEmployee = c.getProductCommission().getEmployee() != null;
        boolean hasProduct = c.getProductCommission().getProduct() != null;

        return new CommissionDetailDTO(
                hasEmployee ? c.getProductCommission().getEmployee().getId() : 0L,
                hasEmployee ? c.getProductCommission().getEmployee().getName() : "N/D",
                c.getId(),
                hasProduct ? c.getProductCommission().getProduct().getId() : 0L,
                hasProduct ? c.getProductCommission().getProduct().getDescription() : "N/D",
                c.getProductCommission().getSaleId(),
                c.getProductCommission().getCreatedAt(),
                c.getInstallment().getId(),
                c.getEquivalentRevenue(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                valorComissaoVirtual,
                BigDecimal.ZERO,
                dataLimite
        );
    }
}