package com.davidantasdev.fieldagentstracking.controller;

import com.davidantasdev.fieldagentstracking.dto.CheckInRequest;
import com.davidantasdev.fieldagentstracking.dto.CheckInResponse;
import com.davidantasdev.fieldagentstracking.dto.SyncHistoryResponse;
import com.davidantasdev.fieldagentstracking.service.CheckInService;
import com.davidantasdev.fieldagentstracking.service.LocationService;
import com.davidantasdev.fieldagentstracking.service.SyncHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/locations")
@Validated
@Tag(name = "Localizações", description = "Gerenciamento de localizações e check-ins")
public class LocationController {

    private final LocationService locationService;
    private final CheckInService checkInService;
    private final SyncHistoryService syncHistoryService;

    public LocationController(LocationService locationService,
                             CheckInService checkInService,
                             SyncHistoryService syncHistoryService) {
        this.locationService = locationService;
        this.checkInService = checkInService;
        this.syncHistoryService = syncHistoryService;
    }

    @PostMapping("/agents/{agentId}/check-ins")
    @Operation(summary = "Registrar check-in manual", description = "Cria um check-in manual com localizacao e notas")
    public ResponseEntity<CheckInResponse> createCheckIn(
            @Parameter(description = "ID do agente")
            @PathVariable Long agentId,
            @Valid @RequestBody CheckInRequest request) {
        CheckInResponse response = checkInService.createCheckIn(agentId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/agents/{agentId}/check-ins")
    @Operation(summary = "Listar check-ins do agente", description = "Lista todos os check-ins do agente com paginacao")
    public Page<CheckInResponse> getCheckIns(
            @Parameter(description = "ID do agente")
            @PathVariable Long agentId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return checkInService.getCheckInsByAgent(agentId, page, size);
    }

    @GetMapping("/agents/{agentId}/check-ins/range")
    @Operation(summary = "Buscar check-ins por periodo", description = "Busca check-ins entre duas datas")
    public List<CheckInResponse> getCheckInsByDateRange(
            @Parameter(description = "ID do agente")
            @PathVariable Long agentId,
            @Parameter(description = "Data/hora de inicio (ISO 8601)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "Data/hora de fim (ISO 8601)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return checkInService.getCheckInsByAgentAndDateRange(agentId, startTime, endTime);
    }

    @GetMapping("/agents/{agentId}/history")
    @Operation(summary = "Historico de localizacoes", description = "Lista todas as localizacoes registradas do agente")
    public ResponseEntity<?> getLocationHistory(
            @Parameter(description = "ID do agente")
            @PathVariable Long agentId) {
        return ResponseEntity.ok(locationService.getAgentLocationHistory(agentId));
    }

    @GetMapping("/agents/{agentId}/last")
    @Operation(summary = "Ultima localizacao", description = "Retorna a ultima localizacao registrada do agente")
    public ResponseEntity<?> getLastLocation(
            @Parameter(description = "ID do agente")
            @PathVariable Long agentId) {
        var lastLocation = locationService.getLastLocationByAgent(agentId);
        return lastLocation != null ? ResponseEntity.ok(lastLocation) : ResponseEntity.noContent().build();
    }

    @GetMapping("/sync-history")
    @Operation(summary = "Historico de sincronizacoes", description = "Lista todas as sincronizacoes com a api gps")
    public Page<SyncHistoryResponse> getSyncHistory(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return syncHistoryService.getSyncHistory(page, size);
    }

    @GetMapping("/sync-history/last")
    @Operation(summary = "Ultima sincronizacao", description = "Retorna dados da ultima sincronizacao realizada")
    public ResponseEntity<SyncHistoryResponse> getLastSync() {
        Optional<SyncHistoryResponse> lastSync = syncHistoryService.getLastSync();
        return lastSync.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/sync-history/range")
    @Operation(summary = "Sincronizacoes por periodo", description = "Busca sincronizacoes entre duas datas")
    public Page<SyncHistoryResponse> getSyncHistoryByRange(
            @Parameter(description = "Data/hora de inicio (ISO 8601)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "Data/hora de fim (ISO 8601)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return syncHistoryService.getSyncHistoryByDateRange(startTime, endTime, page, size);
    }

    @GetMapping("/sync-history/stats")
    @Operation(summary = "Estatisticas de sincronizacao", description = "Retorna metricas e estatisticas do sistema de sincronizacao")
    public ResponseEntity<?> getSyncStats() {
        return ResponseEntity.ok(syncHistoryService.getSyncStats());
    }
}
