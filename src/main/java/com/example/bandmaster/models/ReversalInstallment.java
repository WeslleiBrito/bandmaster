package com.example.bandmaster.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "comissao_estorno_parcela")
public class ReversalInstallment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estorno_id", nullable = false)
    private CommissionReversal commissionReversal;

    @Column(name = "valor_pago", nullable = false, precision = 12, scale = 2)
    private BigDecimal paidValue;

    @Column(name = "data_pagamento", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "observacao")
    private String observation;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime createdAt;
}