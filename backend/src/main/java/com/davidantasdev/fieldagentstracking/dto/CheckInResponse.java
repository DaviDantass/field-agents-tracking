package com.davidantasdev.fieldagentstracking.dto;

import java.time.LocalDateTime;

public record CheckInResponse(
        Long id,
        Long agentId,
        Double latitude,
        Double longitude,
        String notes,
        LocalDateTime createdAt
) {}
