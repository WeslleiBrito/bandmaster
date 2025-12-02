package com.example.bandmaster.models;


import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "funcionario")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fun_cod")
    private Long id;

    @Column(name = "fun_nome", nullable = false)
    private String name;

    @Column(name = "fun_funcao")
    private Integer roleCode;

    // Relacionamento reverso será mapeado na classe ProductCommission
}
