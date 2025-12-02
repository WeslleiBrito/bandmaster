package com.example.bandmaster.models;


import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "receber_historico")
public class ReceivableHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recebhist_cod")
    private Long id;

    // Relacionamento com a Parcela (hist_parcela)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hist_parcela")
    private ReceivableInstallment installment;

    // Relacionamento com FormaPagamento (opcional, pois já tem na parcela, mas existe no banco)
    @Column(name = "hist_forma")
    private Long paymentMethodId;

    @Column(name = "hist_banco")
    private Long bankId; // FK para conta_empresa

    @Column(name = "hist_dtpagamento")
    private LocalDate paymentDate;

    // Convertido Float do Python para BigDecimal no Java para evitar erros de moeda
    @Column(name = "hist_valor")
    private BigDecimal value = BigDecimal.ZERO;

    @Column(name = "hist_usuario")
    private Long userId; // FK para usuario

    @Column(name = "hist_dtcadastro")
    private LocalDate registrationDate;

    @Column(name = "hist_status", length = 1)
    private String status; // 'R' = Recebido, 'E' = Estornado

    @Column(name = "hist_dtestorno")
    private LocalDate reversalDate;

    @Column(name = "hist_acrescimo")
    private BigDecimal addition = BigDecimal.ZERO;

    @Column(name = "hist_desconto")
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "idcnpj", length = 14)
    private String cnpj;

    @Column(name = "sincronizado")
    private Integer synchronizedStatus;

    @Column(name = "dhalteracao")
    private LocalDateTime updatedAt;

    @Column(name = "hist_caixa")
    private Integer cashRegisterId;

    @Column(name = "hist_origem", length = 1)
    private String origin = "O";

    @Column(name = "txId", length = 35)
    private String transactionId;
}