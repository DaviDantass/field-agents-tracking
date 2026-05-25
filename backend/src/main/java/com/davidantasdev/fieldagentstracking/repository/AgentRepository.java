package com.davidantasdev.fieldagentstracking.repository;

import com.davidantasdev.fieldagentstracking.entity.Agent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    Page<Agent> findByActiveTrue(Pageable pageable);

    Page<Agent> findByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);

    Optional<Agent> findByIdAndActiveTrue(Long id);

    Optional<Agent> findByNameIgnoreCaseAndActiveTrue(String name);

    Optional<Agent> findByNameIgnoreCase(String name);
}
