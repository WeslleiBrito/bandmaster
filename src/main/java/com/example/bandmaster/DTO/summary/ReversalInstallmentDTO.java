package com.example.bandmaster.DTO.summary;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReversalInstallmentDTO(
        Long id,
        BigDecimal paidValue,
        LocalDate paymentDate,
        String observation
) {}

