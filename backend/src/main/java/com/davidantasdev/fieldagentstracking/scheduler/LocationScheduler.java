package com.davidantasdev.fieldagentstracking.scheduler;

import com.davidantasdev.fieldagentstracking.service.LocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.schedulers.sync-locations", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class LocationScheduler {

    private final LocationService locationService;

    public LocationScheduler(LocationService locationService) {
        this.locationService = locationService;
    }

    @Scheduled(fixedRateString = "${app.schedulers.sync-locations.interval:300000}")
    public void syncLocations() {
        log.info("Iniciando scheduler 1: sincronizacao de localizacoes gps...");
        try {
            locationService.syncLocationsFromGps();
            log.info("Scheduler 1 concluido com sucesso");
        } catch (Exception e) {
            log.error("Erro no scheduler 1 - sincronizacao de localizacoes", e);
        }
    }
}
