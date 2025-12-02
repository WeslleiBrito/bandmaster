package com.example.bandmaster.repository;

import com.example.bandmaster.models.CommissionPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommissionPaymentRepository extends JpaRepository<CommissionPayment, Long> {

    // Listar pagamentos de uma parcela de comissão específica
    List<CommissionPayment> findByCommissionInstallmentId(Long installmentId);
}

