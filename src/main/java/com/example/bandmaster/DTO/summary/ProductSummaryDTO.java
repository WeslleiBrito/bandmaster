package com.example.bandmaster.DTO.summary;

import java.math.BigDecimal;

public record ProductSummaryDTO(
        Long id,
        String description,
        BigDecimal unitValue
) {}
