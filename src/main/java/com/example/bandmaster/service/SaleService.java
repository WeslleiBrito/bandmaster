package com.example.bandmaster.service;

import com.example.bandmaster.models.Sale;
import com.example.bandmaster.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SaleService {

    @Autowired
    private SaleRepository saleRepository;

    public List<Sale> findAll() {
        return saleRepository.findAll();
    }

    public Page<Sale> findAll(Pageable pageable) {
        return saleRepository.findAll(pageable);
    }

    public Sale findById(Integer id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada"));
    }
}
