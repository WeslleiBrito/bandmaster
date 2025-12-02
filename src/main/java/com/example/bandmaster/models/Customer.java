package com.example.bandmaster.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "cliente")

public class Customer {
    @Id
    @Column(name = "cli_cod")
    private Long id;

    @Column(name = "cli_nome", length = 120)
    private String nome;

    @Column(name = "cli_nomecurto", length = 120)
    private String nomeCurto;

    @Column(name = "cli_cnpj", length = 14)
    private String cnpj;

}
