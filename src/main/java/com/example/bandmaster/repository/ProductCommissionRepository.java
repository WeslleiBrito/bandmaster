package com.example.bandmaster.repository;

import com.example.bandmaster.models.ProductCommission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductCommissionRepository extends JpaRepository<ProductCommission, Long> {

    // Buscar todas as comissões de uma venda específica (vend_cod)
    List<ProductCommission> findBySaleId(Long saleId);

    // Buscar comissões de um vendedor específico
    List<ProductCommission> findByEmployeeId(Long employeeId);

    // Buscar comissões que ainda não foram processadas
    List<ProductCommission> findByProcessedFalse();

    // Exemplo de busca por intervalo de datas (muito comum em relatórios)
    // SQL: SELECT * FROM comissao_produto WHERE created_at BETWEEN ? AND ?
    List<ProductCommission> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Se precisar de algo muito complexo, você pode escrever JPQL (SQL orientado a objetos)
    // Exemplo: Buscar comissões de um vendedor acima de um certo valor
    @Query("SELECT c FROM ProductCommission c WHERE c.employee.id = :empId AND c.revenue > :minRevenue")
    List<ProductCommission> findHighValueCommissions(@Param("empId") Long employeeId,
                                                     @Param("minRevenue") BigDecimal minRevenue);
}