package com.example.bandmaster.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "comissao_abatimento_vendedor")
public class SalespersonReversalDeduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Relaciona com a comissão original que gerou a dívida
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comissao_parcela_id", nullable = false)
    private CommissionInstallment commissionInstallment;

    @Column(name = "valor_abatido", nullable = false, precision = 15, scale = 2)
    private BigDecimal deductedValue = BigDecimal.ZERO;

    @Column(name = "data_abatimento", nullable = false)
    private LocalDate deductionDate;

    @Column(name = "observacao")
    private String observation;
}