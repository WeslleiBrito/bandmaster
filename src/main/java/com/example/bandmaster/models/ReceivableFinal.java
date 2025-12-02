package com.example.bandmaster.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "receber_definitivo")
public class ReceivableFinal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "receb_venda", nullable = false)
    private Integer saleCode;

    @Column(name = "data_doc")
    private LocalDate documentDate;

    @Column(name = "documento", nullable = false)
    private Integer documentNumber;

    @Column(name = "ativo", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    // Reverse Relationship (ManyToOne)
    @ManyToOne
    @JoinColumn(
            name = "receb_venda",
            referencedColumnName = "vend_cod",
            insertable = false,
            updatable = false
    )
    private Sale sale;
}
