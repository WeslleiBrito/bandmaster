package com.example.bandmaster.controller;

import com.example.bandmaster.service.CommissionProcessingService;
import com.example.bandmaster.service.CommissionReportCache;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/commissions")
@RequiredArgsConstructor
public class CommissionController {

    private final CommissionProcessingService commissionService; // Ainda usado para fallback se necessário
    private final CommissionReportCache reportCache;

    @GetMapping("/detail")
    public ResponseEntity<?> getDetail() {
        // Cenário 1: O Cache já tem dados (Resposta Instantânea)
        if (reportCache.hasData()) {
            return ResponseEntity.ok(reportCache.getCachedReport());
        }

        // Cenário 2: Servidor acabou de reiniciar e o Job ainda não rodou.
        // Opção A: Retornar vazio e o front tenta de novo depois.
        // Opção B: Forçar o cálculo agora (vai demorar 22s, mas o usuário vê o dado).
        // Vamos na Opção B para garantir que funcione na primeira vez:
        return ResponseEntity.ok(commissionService.getDetailedCommissionsMap());
    }
}