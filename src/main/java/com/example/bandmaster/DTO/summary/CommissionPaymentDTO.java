package com.example.bandmaster.DTO.summary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Nível 3: O pagamento individual (Folha da árvore)
public record CommissionPaymentDTO(
        Long id,
        LocalDate paymentDate,
        BigDecimal paidValue,
        String observation,
        LocalDateTime createdAt
) {}



