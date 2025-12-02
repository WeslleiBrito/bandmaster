package com.example.bandmaster.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "comissao_status")
public class CommissionStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nome", nullable = false, length = 100)
    private String name;

    @Column(name = "descricao")
    private String description;
}