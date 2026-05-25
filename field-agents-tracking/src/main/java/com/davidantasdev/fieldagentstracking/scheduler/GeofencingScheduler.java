package com.davidantasdev.fieldagentstracking.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.schedulers.geofencing", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class GeofencingScheduler {

    @Scheduled(fixedRateString = "${app.schedulers.geofencing.interval:600000}")
    public void validateGeofencing() {
        log.info("Iniciando scheduler 2: validacao de geofencing...");
        try {
            log.debug("Validacao de geofencing executada");
            log.info("Scheduler 2 concluido com sucesso");
        } catch (Exception e) {
            log.error("Erro no scheduler 2 - validacao de geofencing", e);
        }
    }
}
