package com.example.bandmaster.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;



@Data
@Entity
@Table(name = "receber_parcelas")
public class ReceivableInstallment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parc_cod")
    private Long id;

    // Como não temos o arquivo 'Receber.py' (cabeçalho da conta), mapeamos apenas o ID por enquanto
    @Column(name = "parc_conta")
    private Long accountId;

    @Column(name = "parc_documento", nullable = false, length = 100)
    private String documentNumber;

    // Relacionamento com FormaPagamento (Foreign Key: parc_forma)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parc_forma")
    private PaymentMethod paymentMethod;

    @Column(name = "parc_vlrdocumento", nullable = false, precision = 15, scale = 2)
    private BigDecimal documentValue = BigDecimal.ZERO;

    @Column(name = "parc_vlrpago", nullable = false, precision = 15, scale = 2)
    private BigDecimal paidValue = BigDecimal.ZERO;

    @Column(name = "parc_vlrtitulo", nullable = false, precision = 15, scale = 2)
    private BigDecimal titleValue = BigDecimal.ZERO;

    @Column(name = "parc_numero")
    private Integer number;

    @Column(name = "parc_cancelada")
    private Integer canceled; // 0 = Não, 1 = Sim

    @Column(name = "parc_dtvencimento")
    private LocalDate dueDate;

    @Column(name = "parc_dtcadastro")
    private LocalDate registrationDate;

    @Column(name = "parc_obs", columnDefinition = "TEXT")
    private String observation;

    @Column(name = "parc_statusdesc", length = 50)
    private String statusDescription;

    @Column(name = "parc_dtpagamento")
    private LocalDate paymentDate;

    // Relacionamento com Históricos (OneToMany)
    @OneToMany(mappedBy = "installment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReceivableHistory> histories;

    // Helper para verificar cancelamento (já que o banco usa 0/1)
    public boolean isCanceled() {
        return this.canceled != null && this.canceled == 1;
    }
}
