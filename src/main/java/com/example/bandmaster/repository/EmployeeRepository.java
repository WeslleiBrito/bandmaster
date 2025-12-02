package com.example.bandmaster.repository;

import com.example.bandmaster.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // O Spring gera o SQL: SELECT * FROM funcionario WHERE fun_nome = ?
    Optional<Employee> findByName(String name);
}
