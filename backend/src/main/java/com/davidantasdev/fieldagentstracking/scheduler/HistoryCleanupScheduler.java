package com.davidantasdev.fieldagentstracking.scheduler;

import com.davidantasdev.fieldagentstracking.repository.LocationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(prefix = "app.schedulers.history-cleanup", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class HistoryCleanupScheduler {

    private final LocationRepository locationRepository;
    private final int retentionDays;

    public HistoryCleanupScheduler(LocationRepository locationRepository,
                                    @Value("${app.schedulers.history-cleanup.retention-days:30}") int retentionDays) {
        this.locationRepository = locationRepository;
        this.retentionDays = retentionDays;
    }

    @Scheduled(cron = "${app.schedulers.history-cleanup.cron:0 0 0 * * *}")
    @Transactional
    public void cleanupOldLocations() {
        log.info("Iniciando scheduler 3: limpeza de historico (retencao: {} dias)...", retentionDays);
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            int deleted = locationRepository.deleteByTimestampBefore(cutoffDate);
            log.info("Scheduler 3 concluido: {} localizacoes removidas anteriores a {}", deleted, cutoffDate);
        } catch (Exception e) {
            log.error("Erro no scheduler 3 - limpeza de historico", e);
        }
    }
}
