package com.example.bandmaster.DTO.summary;

import java.math.BigDecimal;
import java.util.List;

// Nível 2: A parcela da comissão (Galho)
public record CommissionInstallmentDTO(
        Long id,
        Long originalInstallmentId, // ID da parcela no contas a receber (receber_parcelas)
        BigDecimal totalCommissionValue,
        BigDecimal equivalentRevenue,
        CommissionStatusDTO status,
        List<CommissionPaymentDTO> payments // Lista aninhada de pagamentos
) {}