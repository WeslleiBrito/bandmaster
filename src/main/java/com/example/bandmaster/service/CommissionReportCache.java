package com.example.bandmaster.service;

import com.example.bandmaster.DTO.summary.CommissionDetailDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class CommissionReportCache {

    // AtomicReference garante que a leitura/escrita seja segura (Thread-safe)
    private final AtomicReference<Map<String, List<CommissionDetailDTO>>> cachedReport = new AtomicReference<>();

    public void updateCache(Map<String, List<CommissionDetailDTO>> newReport) {
        cachedReport.set(newReport);
    }

    public Map<String, List<CommissionDetailDTO>> getCachedReport() {
        Map<String, List<CommissionDetailDTO>> report = cachedReport.get();
        // Se o cache estiver vazio (servidor acabou de ligar), retorna mapa vazio ou nulo
        return report != null ? report : Collections.emptyMap();
    }

    public boolean hasData() {
        return cachedReport.get() != null;
    }
}