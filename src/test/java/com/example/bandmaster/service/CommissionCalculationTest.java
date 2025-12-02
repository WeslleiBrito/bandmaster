package com.example.bandmaster.service;

import com.example.bandmaster.models.*;
import com.example.bandmaster.models.ReceivableInstallment;
import com.example.bandmaster.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles; // Importante

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

// @ActiveProfiles("test") faria o Spring carregar o application-test.properties
// SE estivéssemos subindo o contexto inteiro com @SpringBootTest.
// Como estamos usando @ExtendWith(MockitoExtension.class), estamos testando APENAS a lógica
// sem subir banco de dados, o que é MUITO mais rápido.

@ExtendWith(MockitoExtension.class)
class CommissionCalculationTest {

    // ... (O restante do código permanece IDÊNTICO ao anterior) ...
    // Como estamos usando Mocks (simulações), o H2 nem sequer é ligado aqui.
    // Isso é um "Teste Unitário" puro.

    @Mock private ProductCommissionRepository productCommissionRepository;
    @Mock private CommissionInstallmentRepository commissionInstallmentRepository;
    @Mock private CommissionStatusRepository statusRepository;
    @Mock private ReceivableInstallmentRepository receivableInstallmentRepository;

    @InjectMocks private CommissionProcessingService service;

    private CommissionStatus statusBloqueada;
    private CommissionStatus statusAguardando;

    @BeforeEach
    void setup() {
        statusBloqueada = new CommissionStatus(); statusBloqueada.setId(1L); statusBloqueada.setName("BLOQUEADA");
        statusAguardando = new CommissionStatus(); statusAguardando.setId(2L); statusAguardando.setName("AGUARDANDO PAGAMENTO");

        lenient().when(statusRepository.findByName("BLOQUEADA")).thenReturn(Optional.of(statusBloqueada));
        lenient().when(statusRepository.findByName("AGUARDANDO PAGAMENTO")).thenReturn(Optional.of(statusAguardando));
    }

    @Test
    @DisplayName("Deve calcular comissão proporcional corretamente")
    void deveCalcularComissaoSimples() {
        // ... (Mesmo código do teste anterior) ...
        ProductCommission comissaoProduto = new ProductCommission();
        comissaoProduto.setId(10L);
        comissaoProduto.setSaleId(500L);
        comissaoProduto.setRevenue(new BigDecimal("1000.00"));
        comissaoProduto.setCommissionPercentage(new BigDecimal("5.00"));
        comissaoProduto.setProcessed(false);

        ReceivableInstallment parcela = new ReceivableInstallment();
        parcela.setId(99L);
        parcela.setTitleValue(new BigDecimal("1000.00"));

        when(productCommissionRepository.findByProcessedFalse()).thenReturn(List.of(comissaoProduto));
        when(receivableInstallmentRepository.findValidParcelsBySaleId(500L)).thenReturn(List.of(parcela));

        service.generateCommissionInstallments();

        ArgumentCaptor<CommissionInstallment> captor = ArgumentCaptor.forClass(CommissionInstallment.class);
        verify(commissionInstallmentRepository).save(captor.capture());

        CommissionInstallment salvo = captor.getValue();
        assertEquals(new BigDecimal("50.0000"), salvo.getTotalCommissionValue());
    }
}