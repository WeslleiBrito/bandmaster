package com.example.bandmaster.repository;

import com.example.bandmaster.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Busca por descrição contendo o texto (ILIKE)
    // SQL: ... WHERE prod_descricao LIKE %?%
    List<Product> findByDescriptionContainingIgnoreCase(String description);
}