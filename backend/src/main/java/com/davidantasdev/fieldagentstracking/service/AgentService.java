package com.davidantasdev.fieldagentstracking.service;

import com.davidantasdev.fieldagentstracking.dto.AgentRequest;
import com.davidantasdev.fieldagentstracking.dto.AgentResponse;
import com.davidantasdev.fieldagentstracking.entity.Agent;
import com.davidantasdev.fieldagentstracking.exception.AgentNotFoundException;
import com.davidantasdev.fieldagentstracking.exception.DuplicateAgentException;
import com.davidantasdev.fieldagentstracking.repository.AgentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AgentService {

    private final AgentRepository repository;

    public AgentService(AgentRepository repository) {
        this.repository = repository;
    }

    public AgentResponse create(AgentRequest request) {
        validateUniqueName(request.name(), null);

        Agent agent = new Agent();
        agent.setName(request.name().trim());

        return toResponse(repository.save(agent));
    }

    public Page<AgentResponse> findAll(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        if (name == null || name.isBlank()) {
            return repository.findByActiveTrue(pageable).map(this::toResponse);
        }

        return repository.findByActiveTrueAndNameContainingIgnoreCase(name.trim(), pageable)
                .map(this::toResponse);
    }

    public AgentResponse findById(Long id) {
        return toResponse(findActiveAgent(id));
    }

    public AgentResponse update(Long id, AgentRequest request) {
        Agent agent = findActiveAgent(id);
        validateUniqueName(request.name(), id);

        agent.setName(request.name().trim());

        return toResponse(repository.save(agent));
    }

    public void delete(Long id) {
        Agent agent = findActiveAgent(id);
        agent.setActive(false);
        repository.save(agent);
    }

    private Agent findActiveAgent(Long id) {
        return repository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AgentNotFoundException(id));
    }

    private void validateUniqueName(String name, Long currentAgentId) {
        repository.findByNameIgnoreCaseAndActiveTrue(name.trim())
                .filter(agent -> currentAgentId == null || !agent.getId().equals(currentAgentId))
                .ifPresent(agent -> {
                    throw new DuplicateAgentException(name);
                });
    }

    private AgentResponse toResponse(Agent agent) {
        return new AgentResponse(
                agent.getId(),
                agent.getName(),
                agent.isActive(),
                agent.getRole(),
                agent.getTeam(),
                agent.getPhone(),
                agent.getEmail(),
                agent.getStatus()
        );
    }
}
