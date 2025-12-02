package com.example.bandmaster.models;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "comissao_pagamento")
public class CommissionPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comissao_parcela_id", nullable = false)
    private CommissionInstallment commissionInstallment;

    @Column(name = "data_pagamento", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "valor_pago", nullable = false, precision = 15, scale = 2)
    private BigDecimal paidValue = BigDecimal.ZERO;

    @Column(name = "observacao")
    private String observation;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime updatedAt;
}