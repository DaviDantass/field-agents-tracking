package com.davidantasdev.fieldagentstracking.dto;

import java.time.LocalDateTime;

public record SyncHistoryResponse(
        Long id,
        LocalDateTime syncStartTime,
        LocalDateTime syncEndTime,
        Integer totalLocationsReceived,
        Integer locationsProcessed,
        Integer locationsSkipped,
        String errorMessage,
        String status,
        String syncToken
) {}
