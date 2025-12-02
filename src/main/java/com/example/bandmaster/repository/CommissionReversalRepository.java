package com.example.bandmaster.repository;

import com.example.bandmaster.models.CommissionReversal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommissionReversalRepository extends JpaRepository<CommissionReversal, Long> {

    // Encontrar estornos de um vendedor
    List<CommissionReversal> findByEmployeeId(Long employeeId);

    // Encontrar estorno vinculado a um pagamento de comissão original
    // (Para saber se um pagamento já foi estornado)
    List<CommissionReversal> findByOriginalPaymentId(Long paymentId);
}