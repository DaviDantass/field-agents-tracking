package com.davidantasdev.fieldagentstracking.repository;

import com.davidantasdev.fieldagentstracking.entity.SyncHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SyncHistoryRepository extends JpaRepository<SyncHistory, Long> {
    Page<SyncHistory> findAllByOrderBySyncStartTimeDesc(Pageable pageable);

    Optional<SyncHistory> findFirstByOrderBySyncStartTimeDesc();

    Page<SyncHistory> findBySyncStartTimeGreaterThanEqualAndSyncStartTimeLessThanEqual(
            LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
}
