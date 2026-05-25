package com.davidantasdev.fieldagentstracking.scheduler;

import com.davidantasdev.fieldagentstracking.service.SyncHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.schedulers.monitoring", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class MonitoringScheduler {

    private final SyncHistoryService syncHistoryService;

    public MonitoringScheduler(SyncHistoryService syncHistoryService) {
        this.syncHistoryService = syncHistoryService;
    }

    @Scheduled(fixedRateString = "${app.schedulers.monitoring.interval:900000}")
    public void monitorHealth() {
        log.info("Iniciando scheduler 4: monitoramento operacional...");
        try {
            var stats = syncHistoryService.getSyncStats();
            var lastSync = syncHistoryService.getLastSync();

            log.info("Estatisticas de sincronizacao:");
            log.info("   Total de sincronizacoes: {}", stats.totalSyncs());
            log.info("   Sincronizacoes bem-sucedidas: {}", stats.successfulSyncs());
            log.info("   Sincronizacoes falhadas: {}", stats.failedSyncs());
            log.info("   Total de localizacoes processadas: {}", stats.totalLocationsProcessed());

            if (lastSync.isPresent()) {
                var sync = lastSync.get();
                log.info("Ultima sincronizacao:");
                log.info("   Status: {}", sync.status());
                log.info("   Horario: {}", sync.syncStartTime());
                log.info("   Processadas: {} | Puladas: {}", sync.locationsProcessed(), sync.locationsSkipped());
            }

            if (stats.failedSyncs() > stats.successfulSyncs()) {
                log.warn("Alerta: taxa de falhas de sincronizacao elevada!");
            }

            log.info("Scheduler 4 concluido com sucesso");
        } catch (Exception e) {
            log.error("Erro no scheduler 4 - monitoramento", e);
        }
    }
}
