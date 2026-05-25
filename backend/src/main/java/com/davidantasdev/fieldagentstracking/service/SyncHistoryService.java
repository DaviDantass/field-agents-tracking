package com.davidantasdev.fieldagentstracking.service;

import com.davidantasdev.fieldagentstracking.dto.SyncHistoryResponse;
import com.davidantasdev.fieldagentstracking.entity.SyncHistory;
import com.davidantasdev.fieldagentstracking.mapper.LocationMapper;
import com.davidantasdev.fieldagentstracking.repository.SyncHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class SyncHistoryService {

    private final SyncHistoryRepository syncHistoryRepository;
    private final LocationMapper locationMapper;

    public SyncHistoryService(SyncHistoryRepository syncHistoryRepository,
                             LocationMapper locationMapper) {
        this.syncHistoryRepository = syncHistoryRepository;
        this.locationMapper = locationMapper;
    }

    public Page<SyncHistoryResponse> getSyncHistory(int page, int size) {
        log.info("Buscando histórico de sincronizações - página: {}, tamanho: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return syncHistoryRepository.findAllByOrderBySyncStartTimeDesc(pageable)
                .map(locationMapper::toSyncHistoryResponse);
    }

    public Optional<SyncHistoryResponse> getLastSync() {
        log.info("Buscando última sincronização");
        return syncHistoryRepository.findFirstByOrderBySyncStartTimeDesc()
                .map(locationMapper::toSyncHistoryResponse);
    }

    public Page<SyncHistoryResponse> getSyncHistoryByDateRange(
            LocalDateTime startTime,
            LocalDateTime endTime,
            int page,
            int size) {
        log.info("Buscando sincronizações entre {} e {}", startTime, endTime);
        Pageable pageable = PageRequest.of(page, size);
        return syncHistoryRepository.findBySyncStartTimeGreaterThanEqualAndSyncStartTimeLessThanEqual(
                startTime, endTime, pageable)
                .map(locationMapper::toSyncHistoryResponse);
    }

    public SyncHistoryStats getSyncStats() {
        log.info("Calculando estatísticas de sincronização");

        Page<SyncHistory> allSyncs = syncHistoryRepository.findAllByOrderBySyncStartTimeDesc(
                PageRequest.of(0, Integer.MAX_VALUE));

        long totalSyncs = allSyncs.getTotalElements();
        long successfulSyncs = allSyncs.getContent().stream()
                .filter(s -> "SUCCESS".equals(s.getStatus()))
                .count();
        long failedSyncs = allSyncs.getContent().stream()
                .filter(s -> "FAILED".equals(s.getStatus()))
                .count();

        long totalLocationsProcessed = allSyncs.getContent().stream()
                .mapToLong(SyncHistory::getLocationsProcessed)
                .sum();

        return new SyncHistoryStats(totalSyncs, successfulSyncs, failedSyncs, totalLocationsProcessed);
    }

    public record SyncHistoryStats(
            long totalSyncs,
            long successfulSyncs,
            long failedSyncs,
            long totalLocationsProcessed
    ) {}
}
