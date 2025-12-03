package com.example.bandmaster.scheduler;


import com.example.bandmaster.service.CommissionProcessingService;
import com.example.bandmaster.service.CommissionReportCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommissionJob {

    private final CommissionProcessingService commissionService;
    private final CommissionReportCache reportCache; // Injeta o Cache

    @Scheduled(fixedDelay = 300000) // 5 minutos
    public void runCommissionProcess() {
        log.info("Job de Comissões iniciado.");

        try {
            // 1. Processamento Pesado (Escrita no Banco)
            commissionService.generateCommissionInstallments();

            // 2. Geração do Relatório para Cache (Leitura Pesada)
            log.info("Atualizando cache do relatório de comissões...");
            long start = System.currentTimeMillis();

            // Chama aquela função pesada que leva 22s
            var relatorioMap = commissionService.getDetailedCommissionsMap();

            // Guarda na memória
            reportCache.updateCache(relatorioMap);

            long duration = System.currentTimeMillis() - start;
            log.info("Cache atualizado com sucesso em {} ms", duration);

        } catch (Exception e) {
            log.error("Falha crítica no job de comissões", e);
        }

        log.info("Job finalizado.");
    }
}