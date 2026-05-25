package com.davidantasdev.fieldagentstracking.controller;

import com.davidantasdev.fieldagentstracking.dto.AgentRequest;
import com.davidantasdev.fieldagentstracking.dto.AgentResponse;
import com.davidantasdev.fieldagentstracking.service.AgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/agents")
@Validated
@Tag(name = "Agentes", description = "Gerenciamento de agentes de campo")
public class AgentController {

    private final AgentService service;

    public AgentController(AgentService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Criar novo agente", description = "Cria um novo agente de campo no sistema")
    @ApiResponse(responseCode = "201", description = "Agente criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "409", description = "Nome de agente já existe")
    public ResponseEntity<AgentResponse> create(@Valid @RequestBody AgentRequest request) {
        AgentResponse response = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar agentes", description = "Lista todos os agentes ativos com paginação e filtros")
    @ApiResponse(responseCode = "200", description = "Lista de agentes")
    public Page<AgentResponse> list(
            @Parameter(description = "Filtrar por nome (busca parcial)")
            @RequestParam(required = false) String name,
            @Parameter(description = "Número da página (começa em 0)")
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "A pagina deve ser maior ou igual a 0") int page,
            @Parameter(description = "Quantidade de registros por página")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "O tamanho deve ser maior ou igual a 1")
            @Max(value = 100, message = "O tamanho deve ser menor ou igual a 100") int size
    ) {
        return service.findAll(name, page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter agente por ID", description = "Busca os detalhes de um agente específico")
    @ApiResponse(responseCode = "200", description = "Dados do agente")
    @ApiResponse(responseCode = "404", description = "Agente não encontrado")
    public ResponseEntity<AgentResponse> getById(
            @Parameter(description = "ID do agente")
            @PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar agente", description = "Atualiza os dados de um agente")
    @ApiResponse(responseCode = "200", description = "Agente atualizado")
    @ApiResponse(responseCode = "404", description = "Agente não encontrado")
    @ApiResponse(responseCode = "409", description = "Nome já existe")
    public ResponseEntity<AgentResponse> update(
            @Parameter(description = "ID do agente")
            @PathVariable Long id,
            @Valid @RequestBody AgentRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar agente", description = "Desativa um agente (soft delete)")
    @ApiResponse(responseCode = "204", description = "Agente desativado")
    @ApiResponse(responseCode = "404", description = "Agente não encontrado")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do agente")
            @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
