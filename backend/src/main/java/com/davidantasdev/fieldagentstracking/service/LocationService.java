package com.davidantasdev.fieldagentstracking.service;

import com.davidantasdev.fieldagentstracking.entity.Agent;
import com.davidantasdev.fieldagentstracking.entity.Location;
import com.davidantasdev.fieldagentstracking.entity.SyncHistory;
import com.davidantasdev.fieldagentstracking.integration.GpsClient;
import com.davidantasdev.fieldagentstracking.repository.AgentRepository;
import com.davidantasdev.fieldagentstracking.repository.LocationRepository;
import com.davidantasdev.fieldagentstracking.repository.SyncHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LocationService {

    private final LocationRepository locationRepository;
    private final AgentRepository agentRepository;
    private final SyncHistoryRepository syncHistoryRepository;
    private final GpsClient gpsClient;

    public LocationService(LocationRepository locationRepository,
                          AgentRepository agentRepository,
                          SyncHistoryRepository syncHistoryRepository,
                          GpsClient gpsClient) {
        this.locationRepository = locationRepository;
        this.agentRepository = agentRepository;
        this.syncHistoryRepository = syncHistoryRepository;
        this.gpsClient = gpsClient;
    }

    @Transactional
    public void syncLocationsFromGps() {
        log.info("Iniciando sincronizacao de agentes da api externa...");

        String lastSyncToken = syncHistoryRepository.findFirstByOrderBySyncStartTimeDesc()
                .map(SyncHistory::getSyncToken)
                .filter(t -> t != null && !t.isBlank())
                .orElse(null);

        if (lastSyncToken != null) {
            log.info("Usando syncToken incremental: {}", lastSyncToken);
        }

        SyncHistory syncHistory = new SyncHistory();
        syncHistory.setSyncStartTime(LocalDateTime.now());
        syncHistory.setTotalLocationsReceived(0);
        syncHistory.setLocationsProcessed(0);
        syncHistory.setLocationsSkipped(0);

        try {
            GpsClient.AgentSyncResult result = gpsClient.fetchAgentsFromExternalAPI(lastSyncToken);
            List<Map<String, Object>> externalAgents = result.agents();

            syncHistory.setSyncToken(result.nextSyncToken());

            if (externalAgents.isEmpty()) {
                log.warn("Nenhum agente recebido da api");
                syncHistory.setStatus("PARTIAL_SUCCESS");
                syncHistory.setErrorMessage("Nenhum agente recebido");
                syncHistory.setSyncEndTime(LocalDateTime.now());
                syncHistoryRepository.save(syncHistory);
                return;
            }

            log.info("{} agentes recebidos. Sincronizando...", externalAgents.size());
            syncHistory.setTotalLocationsReceived(externalAgents.size());

            for (Map<String, Object> agentData : externalAgents) {
                try {
                    syncExternalAgent(agentData, syncHistory);
                } catch (Exception e) {
                    log.error("Erro ao sincronizar agente: {}", agentData, e);
                    syncHistory.setLocationsSkipped(syncHistory.getLocationsSkipped() + 1);
                }
            }

            syncHistory.setStatus("SUCCESS");
            log.info("Sincronizacao concluida! Processados: {}, Pulados: {}",
                    syncHistory.getLocationsProcessed(),
                    syncHistory.getLocationsSkipped());

        } catch (Exception e) {
            log.error("Erro critico na sincronizacao", e);
            syncHistory.setStatus("FAILED");
            syncHistory.setErrorMessage(e.getMessage());
        } finally {
            syncHistory.setSyncEndTime(LocalDateTime.now());
            syncHistoryRepository.save(syncHistory);
        }
    }

    private void syncExternalAgent(Map<String, Object> agentData, SyncHistory syncHistory) {
        String externalId = (String) agentData.get("id");
        String name = (String) agentData.get("name");
        Boolean active = (Boolean) agentData.get("active");

        if (externalId == null || name == null) {
            log.warn("Dados de agente incompletos: {}", agentData);
            syncHistory.setLocationsSkipped(syncHistory.getLocationsSkipped() + 1);
            return;
        }

        Agent agent = agentRepository.findByNameIgnoreCase(name).orElse(null);

        if (agent == null) {
            agent = new Agent();
            agent.setName(name);
        }

        agent.setActive(active != null ? active : true);
        agent.setRole((String) agentData.get("role"));
        agent.setTeam((String) agentData.get("team"));
        agent.setPhone((String) agentData.get("phone"));
        agent.setEmail((String) agentData.get("email"));
        agent.setStatus((String) agentData.get("status"));
        agentRepository.save(agent);

        Double latitude = toDouble(agentData.get("latitude"));
        Double longitude = toDouble(agentData.get("longitude"));
        if (latitude != null && longitude != null) {
            saveAgentLocation(agent, latitude, longitude);
        }

        syncHistory.setLocationsProcessed(syncHistory.getLocationsProcessed() + 1);
        log.debug("Agente sincronizado: {} (ID externo: {})", name, externalId);
    }

    private void saveAgentLocation(Agent agent, Double latitude, Double longitude) {
        LocalDateTime now = LocalDateTime.now();
        if (!locationRepository.existsByAgentIdAndTimestamp(agent.getId(), now)) {
            Location location = new Location();
            location.setAgent(agent);
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setTimestamp(now);
            locationRepository.save(location);
        }
    }

    private Double toDouble(Object value) {
        if (value instanceof Number n) return n.doubleValue();
        if (value instanceof String s) {
            try { return Double.parseDouble(s); } catch (NumberFormatException e) { return null; }
        }
        return null;
    }

    public List<Location> getAgentLocationHistory(Long agentId) {
        return locationRepository.findByAgentId(agentId);
    }

    public Location getLastLocationByAgent(Long agentId) {
        return locationRepository.findLastLocationByAgentId(agentId);
    }
}
