package com.example.bandmaster.DTO.summary;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Nível 1: A comissão do produto (Raiz)
public record ProductCommissionDTO(
        Long id,
        Long saleId, // ID da Venda
        ProductSummaryDTO product,
        EmployeeSummaryDTO employee,

        // Valores financeiros
        BigDecimal cost,
        BigDecimal revenue,
        BigDecimal commissionPercentage,
        BigDecimal canceledQuantity,

        Boolean processed,
        LocalDateTime createdAt,

        // Lista aninhada de parcelas
        List<CommissionInstallmentDTO> installments
) {}