package com.example.bandmaster.repository;

import com.example.bandmaster.models.SalespersonReversalDeduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SalespersonReversalDeductionRepository extends JpaRepository<SalespersonReversalDeduction, Long> {
    // Busca todos os pagamentos que o vendedor fez para abater uma comissão específica
    List<SalespersonReversalDeduction> findByCommissionInstallmentId(Long commissionInstallmentId);
}