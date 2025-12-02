package com.example.bandmaster.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "produto")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prod_cod")
    private Long id;

    @Column(name = "prod_descricao", nullable = false)
    private String description;

    @Column(name = "prod_vrunit", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitValue;
}