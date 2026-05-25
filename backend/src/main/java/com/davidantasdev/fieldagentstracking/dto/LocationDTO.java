package com.davidantasdev.fieldagentstracking.dto;

public record LocationDTO(
        Long agentId,
        Double latitude,
        Double longitude,
        String timestamp
) {}
