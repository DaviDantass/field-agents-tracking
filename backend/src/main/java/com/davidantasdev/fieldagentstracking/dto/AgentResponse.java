package com.davidantasdev.fieldagentstracking.dto;

public record AgentResponse(
        Long id,
        String name,
        boolean active,
        String role,
        String team,
        String phone,
        String email,
        String status
) {}
