package com.example.bandmaster.repository;

import com.example.bandmaster.models.CommissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommissionStatusRepository extends JpaRepository<CommissionStatus, Long> {
    // Útil para pegar o status "PENDENTE" ou "PAGO" pelo nome
    Optional<CommissionStatus> findByName(String name);
}