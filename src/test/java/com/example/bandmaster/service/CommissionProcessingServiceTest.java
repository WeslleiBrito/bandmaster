package com.example.bandmaster.service;

import com.example.bandmaster.DTO.summary.CommissionDetailDTO;
import com.example.bandmaster.models.*;
import com.example.bandmaster.models.ReceivableHistory;
import com.example.bandmaster.models.ReceivableInstallment;
import com.example.bandmaster.repository.*;
import com.example.bandmaster.util.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommissionProcessingServiceTest {

    @Mock private ProductCommissionRepository productCommissionRepository;
    @Mock private CommissionInstallmentRepository commissionInstallmentRepository;
    @Mock private CommissionStatusRepository statusRepository;
    @Mock private ReceivableInstallmentRepository receivableInstallmentRepository;
    @Mock private ReceivableHistoryRepository receivableHistoryRepository;
    @Mock private SalespersonReversalDeductionRepository deductionRepository;
    @Mock private DateUtils dateUtils;

    @InjectMocks
    private CommissionProcessingService service;

    private CommissionStatus stAguardando, stBloqueada;

    @BeforeEach
    void setup() {
        stAguardando = new CommissionStatus(); stAguardando.setId(1L); stAguardando.setName("AGUARDANDO PAGAMENTO");
        stBloqueada = new CommissionStatus(); stBloqueada.setId(2L); stBloqueada.setName("BLOQUEADA");
    }

    // ============================================================================================
    // PARTE 1: GERAÇÃO (INSERTS)
    // ============================================================================================

    @Test
    @DisplayName("Geração: Deve salvar registro inicial no banco")
    void deveGerarRegistroInicial() {
        lenient().when(statusRepository.findByName("AGUARDANDO PAGAMENTO")).thenReturn(Optional.of(stAguardando));
        lenient().when(statusRepository.findByName("BLOQUEADA")).thenReturn(Optional.of(stBloqueada));

        ProductCommission pc = createProductCommission(100L, new BigDecimal("1000.00"), new BigDecimal("10.00"));
        ReceivableInstallment parc = createReceivableParcel(50L, new BigDecimal("1000.00"));
        parc.getPaymentMethod().setTerm("P");

        when(productCommissionRepository.findByProcessedFalse()).thenReturn(List.of(pc));
        when(receivableInstallmentRepository.findValidParcelsBySaleId(any())).thenReturn(List.of(parc));

        service.generateCommissionInstallments();

        ArgumentCaptor<CommissionInstallment> captor = ArgumentCaptor.forClass(CommissionInstallment.class);
        verify(commissionInstallmentRepository).save(captor.capture());

        assertEquals(new BigDecimal("100.0000"), captor.getValue().getTotalCommissionValue());
    }

    // ============================================================================================
    // PARTE 2: REGRAS DE STATUS (Baseadas no Python)
    // ============================================================================================

    @Test
    @DisplayName("Regra ESTORNADA (Python): Vendedor recebeu mais do que o Líquido da Venda")
    void regraEstornadaPython() {
        // Cenário Crítico:
        // Venda: R$ 100,00 | Comissão: R$ 10,00
        CommissionInstallment c = createBaseCommission("10.00", "100.00");

        // 1. Cliente pagou tudo (R$ 100,00)
        // 2. Cliente estornou quase tudo (R$ 95,00) -> Sobrou R$ 5,00 no caixa (Líquido)
        ReceivableHistory h1 = createHistory("100.00", LocalDate.now(), "R");
        ReceivableHistory h2 = createHistory("95.00", LocalDate.now(), "E");

        // 3. O Vendedor JÁ RECEBEU a comissão cheia (R$ 10,00) antes do estorno
        CommissionPayment cp = new CommissionPayment();
        cp.setPaidValue(new BigDecimal("10.00"));
        c.setPayments(List.of(cp));

        // 4. Nenhum abatimento feito ainda
        when(deductionRepository.findByCommissionInstallmentId(any())).thenReturn(Collections.emptyList());

        when(commissionInstallmentRepository.findAllWithDetails()).thenReturn(List.of(c));
        when(receivableHistoryRepository.findByInstallmentId(any())).thenReturn(List.of(h1, h2));

        Map<String, List<CommissionDetailDTO>> result = service.getDetailedCommissionsMap();

        // Lógica Python:
        // Valor Estornado (95) > 0 ? SIM.
        // Comissão Paga (10) > Valor Líquido (5) ? SIM.
        // Status -> ESTORNADA

        assertFalse(result.get("ESTORNADA").isEmpty(), "Deveria estar em ESTORNADA pela regra do Python");
        assertEquals(1, result.get("ESTORNADA").size());

        // O valor mostrado deve ser o Saldo Devedor (R$ 10,00)
        assertEquals(new BigDecimal("10.00"), result.get("ESTORNADA").get(0).valorComissaoTotal());
    }

    @Test
    @DisplayName("Regra AGUARDANDO (Python): Valor Líquido <= 0")
    void regraAguardandoPython() {
        CommissionInstallment c = createBaseCommission("10.00", "100.00");

        // Cenário: Pagou 100, Estornou 100. Líquido = 0.
        ReceivableHistory h1 = createHistory("100.00", LocalDate.now(), "R");
        ReceivableHistory h2 = createHistory("100.00", LocalDate.now(), "E");

        // Vendedor NÃO recebeu nada ainda
        when(commissionInstallmentRepository.findAllWithDetails()).thenReturn(List.of(c));
        when(receivableHistoryRepository.findByInstallmentId(any())).thenReturn(List.of(h1, h2));

        Map<String, List<CommissionDetailDTO>> result = service.getDetailedCommissionsMap();

        // Lógica Python: valor_liquido (0) <= 0 -> AGUARDANDO PAGAMENTO
        assertFalse(result.get("AGUARDANDO PAGAMENTO").isEmpty());
        assertEquals(new BigDecimal("10.00"), result.get("AGUARDANDO PAGAMENTO").get(0).valorComissaoTotal());
    }

    @Test
    @DisplayName("Regra PAGA (Python): Pago ao Vendedor >= Total Comissão")
    void regraPagaPython() {
        CommissionInstallment c = createBaseCommission("10.00", "100.00");

        // Cliente pagou, tudo certo
        ReceivableHistory h = createHistory("100.00", LocalDate.now(), "R");

        // Vendedor recebeu R$ 10,00
        CommissionPayment cp = new CommissionPayment();
        cp.setPaidValue(new BigDecimal("10.00"));
        c.setPayments(List.of(cp));

        when(commissionInstallmentRepository.findAllWithDetails()).thenReturn(List.of(c));
        when(receivableHistoryRepository.findByInstallmentId(any())).thenReturn(List.of(h));

        Map<String, List<CommissionDetailDTO>> result = service.getDetailedCommissionsMap();

        // Lógica Python: valor_comissao_paga (10) >= valor_comissao_total (10) -> PAGA
        assertEquals(1, result.get("PAGA").size());
    }

    @Test
    @DisplayName("Regra LIBERADA (Python): Líquido > 0 e Data Atingida")
    void regraLiberadaPython() {
        CommissionInstallment c = createBaseCommission("10.00", "100.00");

        // Pagou R$ 100,00 mês passado
        ReceivableHistory h = createHistory("100.00", LocalDate.now().minusDays(30), "R");

        when(commissionInstallmentRepository.findAllWithDetails()).thenReturn(List.of(c));
        when(receivableHistoryRepository.findByInstallmentId(any())).thenReturn(List.of(h));

        // DateUtils (5º dia útil) = ONTEM
        when(dateUtils.getFifthBusinessDayOfNextMonth(any())).thenReturn(LocalDate.now().minusDays(1));

        Map<String, List<CommissionDetailDTO>> result = service.getDetailedCommissionsMap();

        // Lógica Python: Else (Líquido > 0) -> Data Limite <= Hoje -> LIBERADA
        assertEquals(1, result.get("LIBERADA").size());
    }

    @Test
    @DisplayName("Regra BLOQUEADA (Python): Líquido > 0 e Data NÃO Atingida")
    void regraBloqueadaPython() {
        CommissionInstallment c = createBaseCommission("10.00", "100.00");

        // Pagou R$ 100,00 hoje
        ReceivableHistory h = createHistory("100.00", LocalDate.now(), "R");

        when(commissionInstallmentRepository.findAllWithDetails()).thenReturn(List.of(c));
        when(receivableHistoryRepository.findByInstallmentId(any())).thenReturn(List.of(h));

        // DateUtils (5º dia útil) = FUTURO
        when(dateUtils.getFifthBusinessDayOfNextMonth(any())).thenReturn(LocalDate.now().plusDays(20));

        Map<String, List<CommissionDetailDTO>> result = service.getDetailedCommissionsMap();

        // Lógica Python: Else (Líquido > 0) -> Data Limite > Hoje -> BLOQUEADA
        assertEquals(1, result.get("BLOQUEADA").size());
    }

    @Test
    @DisplayName("Split Virtual: 60% Pago -> 60% Bloqueada + 40% Aguardando")
    void testeSplitVirtual() {
        CommissionInstallment c = createBaseCommission("10.00", "100.00");
        ReceivableHistory h = createHistory("60.00", LocalDate.now(), "R"); // 60%

        when(commissionInstallmentRepository.findAllWithDetails()).thenReturn(List.of(c));
        when(receivableHistoryRepository.findByInstallmentId(any())).thenReturn(List.of(h));
        when(dateUtils.getFifthBusinessDayOfNextMonth(any())).thenReturn(LocalDate.now().plusDays(20));

        Map<String, List<CommissionDetailDTO>> result = service.getDetailedCommissionsMap();

        assertEquals(new BigDecimal("6.00"), result.get("BLOQUEADA").get(0).valorComissaoTotal());
        assertEquals(new BigDecimal("4.00"), result.get("AGUARDANDO PAGAMENTO").get(0).valorComissaoTotal());
    }

    // --- HELPER METHODS ---
    private CommissionInstallment createBaseCommission(String valorComissao, String valorParcela) {
        CommissionInstallment c = new CommissionInstallment();
        c.setId(1L);
        c.setTotalCommissionValue(new BigDecimal(valorComissao));
        c.setInstallment(new ReceivableInstallment());
        c.getInstallment().setId(50L);
        c.getInstallment().setTitleValue(new BigDecimal(valorParcela));

        ProductCommission pc = new ProductCommission();
        pc.setCreatedAt(LocalDateTime.now());
        pc.setEmployee(new Employee()); pc.getEmployee().setId(1L); pc.getEmployee().setName("Vendedor");
        pc.setProduct(new Product()); pc.getProduct().setId(10L); pc.getProduct().setDescription("Prod");
        c.setProductCommission(pc);

        return c;
    }

    private ProductCommission createProductCommission(Long id, BigDecimal rev, BigDecimal pct) {
        ProductCommission pc = new ProductCommission();
        pc.setId(id);
        pc.setSaleId(999L);
        pc.setRevenue(rev);
        pc.setCommissionPercentage(pct);
        pc.setCreatedAt(LocalDateTime.now());
        pc.setEmployee(new Employee());
        pc.setProduct(new Product());
        return pc;
    }

    private ReceivableInstallment createReceivableParcel(Long id, BigDecimal value) {
        ReceivableInstallment ri = new ReceivableInstallment();
        ri.setId(id);
        ri.setTitleValue(value);
        PaymentMethod pm = new PaymentMethod();
        pm.setTerm("P");
        ri.setPaymentMethod(pm);
        return ri;
    }

    private ReceivableHistory createHistory(String valor, LocalDate data, String status) {
        ReceivableHistory h = new ReceivableHistory();
        h.setValue(new BigDecimal(valor));
        h.setPaymentDate(data);
        h.setStatus(status);
        return h;
    }
}