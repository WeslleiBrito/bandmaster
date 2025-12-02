package com.example.bandmaster.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "comissao_estorno")
public class CommissionReversal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comissao_pagamento_id", nullable = false)
    private CommissionPayment originalPayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Employee employee;

    @Column(name = "valor_estornado", nullable = false, precision = 12, scale = 2)
    private BigDecimal reversedValue;

    @Column(name = "data_estorno", nullable = false)
    private LocalDate reversalDate;

    @Column(name = "motivo")
    private String reason;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime updatedAt;

    // Relacionamento com as parcelas do estorno (o pagamento da dívida do estorno)
    @OneToMany(mappedBy = "commissionReversal", cascade = CascadeType.ALL)
    private List<ReversalInstallment> reversalInstallments;

    /**
     * Lógica transiente para calcular status, similar à property @property do Python.
     * Não é salva no banco, apenas calculada em memória.
     */
    @Transient
    public String getStatus() {
        BigDecimal totalPaid = BigDecimal.ZERO;
        if (reversalInstallments != null) {
            totalPaid = reversalInstallments.stream()
                    .map(ReversalInstallment::getPaidValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        if (totalPaid.compareTo(BigDecimal.ZERO) == 0) {
            return "PENDING";
        } else if (totalPaid.compareTo(reversedValue) < 0) {
            return "PARTIAL";
        } else {
            return "PAID";
        }
    }
}