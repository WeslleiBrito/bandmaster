package com.example.bandmaster.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "comissao_parcela")
public class CommissionInstallment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comissao_produto_id", nullable = false)
    private ProductCommission productCommission;

    // --- MUDANÇA AQUI ---
    // Antes: private Long installmentId;
    // Agora: Mapeamento completo para permitir JOIN FETCH
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcela_id", nullable = false)
    private ReceivableInstallment installment;
    // --------------------

    @Column(name = "valor_comissao_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalCommissionValue = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private CommissionStatus status;

    @CreationTimestamp
    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "data_atualizacao")
    private LocalDateTime updatedAt;

    @Column(name = "faturamento_equivalente", nullable = false, precision = 15, scale = 4)
    private BigDecimal equivalentRevenue = BigDecimal.ZERO;

    @OneToMany(mappedBy = "commissionInstallment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommissionPayment> payments;
}