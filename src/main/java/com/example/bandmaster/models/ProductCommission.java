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
@Table(name = "comissao_produto")
public class ProductCommission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Mapeamento direto de ID pois a entidade Venda não foi fornecida
    @Column(name = "vend_cod", nullable = false)
    private Long saleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prod_cod", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor", nullable = false)
    private Employee employee;

    @Column(name = "desp_fx", precision = 11, scale = 2)
    private BigDecimal fixedExpense = BigDecimal.ZERO;

    @Column(name = "custo", precision = 15, scale = 2)
    private BigDecimal cost = BigDecimal.ZERO;

    @Column(name = "faturamento", precision = 15, scale = 2)
    private BigDecimal revenue = BigDecimal.ZERO;

    @Column(name = "qtd_cancelada", precision = 15, scale = 2)
    private BigDecimal canceledQuantity = BigDecimal.ZERO;

    @Column(name = "desp_vr_percentual", precision = 11, scale = 2)
    private BigDecimal percentageExpenseValue = BigDecimal.ZERO;

    @Column(name = "comissao_percentual", nullable = false, precision = 15, scale = 2)
    private BigDecimal commissionPercentage;

    @Column(name = "processada")
    private Boolean processed;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "productCommission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommissionInstallment> installments;
}

