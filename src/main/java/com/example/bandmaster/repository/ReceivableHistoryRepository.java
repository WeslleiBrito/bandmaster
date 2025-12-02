package com.example.bandmaster.repository;

import com.example.bandmaster.models.ReceivableHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceivableHistoryRepository extends JpaRepository<ReceivableHistory, Long> {

    // CORREÇÃO: Mudamos de 'findByParcelId' para 'findByInstallmentId'
    // O Spring vai buscar: Atributo 'installment' -> Atributo 'id'
    List<ReceivableHistory> findByInstallmentId(Long installmentId);
}