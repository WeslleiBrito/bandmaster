package com.example.bandmaster.models;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "venda")
public class Sale {

    @Id
    @Column(name = "vend_cod")
    private Integer id;

    @Column(name = "data", nullable = false)
    private LocalDate date;

    @Column(name = "hora")
    private LocalTime time;

    @Column(name = "cliente", nullable = false)
    private Integer customerId;

    @Column(name = "vendedor")
    private Integer sellerId;

    @Column(name = "total")
    private Double total = 0.0;

    @Column(name = "pix", nullable = false)
    private BigDecimal pix = BigDecimal.ZERO;

    @Column(name = "cartao", nullable = false)
    private BigDecimal card = BigDecimal.ZERO;

    @Column(name = "cartaod", nullable = false)
    private BigDecimal debitCard = BigDecimal.ZERO;

    @Column(name = "dinheiro", nullable = false)
    private BigDecimal cash = BigDecimal.ZERO;

    @Column(name = "cheque", nullable = false)
    private BigDecimal checkValue = BigDecimal.ZERO;

    @Column(name = "prazo", nullable = false)
    private BigDecimal termValue = BigDecimal.ZERO;

    @Column(name = "transferencia", nullable = false)
    private BigDecimal transfer = BigDecimal.ZERO;

    @Column(name = "credito", nullable = false)
    private BigDecimal credit = BigDecimal.ZERO;

    @Column(name = "ticket", nullable = false)
    private BigDecimal ticket = BigDecimal.ZERO;

    @Column(name = "status")
    private Integer status = 0;

    @Column(name = "observacao")
    private String note;

    @Column(name = "finalizacao")
    private LocalDateTime finalizedAt;

    @Column(name = "processada")
    private Integer processed = 0;

    @Column(name = "gerarcomissao")
    private Integer generateCommission = 1;

    // Relationship: Sale → ReceberDefinitivo (OneToMany)
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<ReceivableFinal> receivables;

}
