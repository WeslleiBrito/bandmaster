package com.example.bandmaster.repository;

import com.example.bandmaster.models.CommissionInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommissionInstallmentRepository extends JpaRepository<CommissionInstallment, Long> {

    // Encontrar parcelas associadas a uma comissão de produto específica
    List<CommissionInstallment> findByProductCommissionId(Long productCommissionId);

    // Encontrar uma parcela de comissão pelo ID da parcela original do financeiro (receber_parcelas)
    // Útil para quando o sistema financeiro notifica que uma parcela foi paga
    Optional<CommissionInstallment> findByInstallmentId(Long installmentId);

    // Buscar parcelas por status (ex: PENDENTE)
    List<CommissionInstallment> findByStatusName(String statusName);

    // Busca otimizada com JOIN FETCH para trazer tudo de uma vez
    @Query("SELECT c FROM CommissionInstallment c " +
            "JOIN FETCH c.productCommission pc " +
            "JOIN FETCH pc.employee " +
            "JOIN FETCH pc.product " +
            "JOIN FETCH c.status " +
            "LEFT JOIN FETCH c.installment " + // LEFT JOIN pois parcela pode ser nula em casos raros
            "WHERE c.installment IS NOT NULL")
    List<CommissionInstallment> findAllWithDetails();
}