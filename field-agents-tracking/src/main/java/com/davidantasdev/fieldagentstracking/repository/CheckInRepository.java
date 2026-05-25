package com.davidantasdev.fieldagentstracking.repository;

import com.davidantasdev.fieldagentstracking.entity.CheckIn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    Page<CheckIn> findByAgentId(Long agentId, Pageable pageable);

    @Query("SELECT c FROM CheckIn c WHERE c.agent.id = :agentId AND c.createdAt >= :startTime AND c.createdAt <= :endTime")
    List<CheckIn> findByAgentIdAndDateRange(@Param("agentId") Long agentId,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);
}
