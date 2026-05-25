package com.davidantasdev.fieldagentstracking.repository;

import com.davidantasdev.fieldagentstracking.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByAgentId(Long agentId);

    @Query("SELECT l FROM Location l WHERE l.agent.id = :agentId AND l.timestamp BETWEEN :start AND :end ORDER BY l.timestamp ASC")
    List<Location> findLocationsByAgentAndPeriod(
            @Param("agentId") Long agentId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT l FROM Location l WHERE l.agent.id = :agentId ORDER BY l.timestamp DESC LIMIT 1")
    Location findLastLocationByAgentId(@Param("agentId") Long agentId);

    boolean existsByAgentIdAndTimestamp(Long agentId, LocalDateTime timestamp);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Location l WHERE l.timestamp < :cutoff")
    int deleteByTimestampBefore(@Param("cutoff") LocalDateTime cutoff);
}
