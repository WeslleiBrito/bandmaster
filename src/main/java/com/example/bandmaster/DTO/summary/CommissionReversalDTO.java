package com.example.bandmaster.DTO.summary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CommissionReversalDTO(
        Long id,
        Long originalPaymentId, // Referência ao ID do pagamento que gerou o estorno
        EmployeeSummaryDTO employee,

        BigDecimal reversedValue,
        BigDecimal totalPaidValue, // Calculado (soma das parcelas)
        LocalDate reversalDate,
        String reason,
        String status, // Calculado (PENDING, PARTIAL, PAID)

        List<ReversalInstallmentDTO> installments
) {}
