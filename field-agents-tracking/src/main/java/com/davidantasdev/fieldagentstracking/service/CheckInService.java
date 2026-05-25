package com.davidantasdev.fieldagentstracking.service;

import com.davidantasdev.fieldagentstracking.dto.CheckInRequest;
import com.davidantasdev.fieldagentstracking.dto.CheckInResponse;
import com.davidantasdev.fieldagentstracking.entity.Agent;
import com.davidantasdev.fieldagentstracking.entity.CheckIn;
import com.davidantasdev.fieldagentstracking.exception.AgentNotFoundException;
import com.davidantasdev.fieldagentstracking.mapper.LocationMapper;
import com.davidantasdev.fieldagentstracking.repository.AgentRepository;
import com.davidantasdev.fieldagentstracking.repository.CheckInRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final AgentRepository agentRepository;
    private final LocationMapper locationMapper;

    public CheckInService(CheckInRepository checkInRepository,
                         AgentRepository agentRepository,
                         LocationMapper locationMapper) {
        this.checkInRepository = checkInRepository;
        this.agentRepository = agentRepository;
        this.locationMapper = locationMapper;
    }

    @Transactional
    public CheckInResponse createCheckIn(Long agentId, CheckInRequest request) {
        log.info("Criando check-in para agente: {}", agentId);

        Agent agent = agentRepository.findByIdAndActiveTrue(agentId)
                .orElseThrow(() -> new AgentNotFoundException(agentId));

        CheckIn checkIn = new CheckIn();
        checkIn.setAgent(agent);
        checkIn.setLatitude(request.latitude());
        checkIn.setLongitude(request.longitude());
        checkIn.setNotes(request.notes().trim());
        checkIn.setCreatedAt(LocalDateTime.now());

        CheckIn saved = checkInRepository.save(checkIn);
        log.info("Check-in criado com sucesso. ID: {}", saved.getId());

        return locationMapper.toCheckInResponse(saved);
    }

    public Page<CheckInResponse> getCheckInsByAgent(Long agentId, int page, int size) {
        log.info("Buscando check-ins do agente: {}", agentId);

        agentRepository.findByIdAndActiveTrue(agentId)
                .orElseThrow(() -> new AgentNotFoundException(agentId));

        Pageable pageable = PageRequest.of(page, size);
        return checkInRepository.findByAgentId(agentId, pageable)
                .map(locationMapper::toCheckInResponse);
    }

    public List<CheckInResponse> getCheckInsByAgentAndDateRange(
            Long agentId,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        log.info("Buscando check-ins do agente {} entre {} e {}", agentId, startTime, endTime);

        agentRepository.findByIdAndActiveTrue(agentId)
                .orElseThrow(() -> new AgentNotFoundException(agentId));

        return checkInRepository.findByAgentIdAndDateRange(agentId, startTime, endTime)
                .stream()
                .map(locationMapper::toCheckInResponse)
                .toList();
    }
}
