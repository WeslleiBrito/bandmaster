package com.example.bandmaster.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "forma_pagamento")
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "formpag_cod")
    private Long id;

    @Column(name = "formpag_descricao", nullable = false, length = 50)
    private String description;

    @Column(name = "formpag_prazo", length = 1)
    private String term; // 'V' for Vista, etc.

    @Column(name = "formpag_tipo", length = 1)
    private String type;
}