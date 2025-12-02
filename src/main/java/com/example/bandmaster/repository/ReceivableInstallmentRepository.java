package com.example.bandmaster.repository;

import com.example.bandmaster.models.ReceivableInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReceivableInstallmentRepository extends JpaRepository<ReceivableInstallment, Long> {

    // CORREÇÃO FEITA:
    // 1. Mudamos "p.saleId" para "p.accountId" (pois é esse o nome do atributo na sua Entity)
    // 2. Mantive o parâmetro como :saleId, pois é o valor que você envia.
    // 3. Onde estava "p.canceled <> 1", adicionei verificação de nulo por segurança.

    @Query("SELECT p FROM ReceivableInstallment p WHERE p.accountId = :saleId AND (p.canceled IS NULL OR p.canceled != 1)")
    List<ReceivableInstallment> findValidParcelsBySaleId(@Param("saleId") Long saleId);
}