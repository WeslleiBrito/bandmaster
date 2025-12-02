package com.example.bandmaster.controller;

import com.example.bandmaster.models.Sale;
import com.example.bandmaster.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sales")
public class SaleController {

    @Autowired
    private SaleService saleService;

    @GetMapping
    public ResponseEntity<List<Sale>> listar() {
        return ResponseEntity.ok(saleService.findAll());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Sale>> listarPaginado(Pageable pageable) {
        return ResponseEntity.ok(saleService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sale> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(saleService.findById(id));
    }

}
