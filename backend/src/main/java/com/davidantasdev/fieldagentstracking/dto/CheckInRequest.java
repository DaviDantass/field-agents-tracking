package com.davidantasdev.fieldagentstracking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckInRequest(
        @NotNull(message = "Latitude é obrigatória")
        Double latitude,

        @NotNull(message = "Longitude é obrigatória")
        Double longitude,

        @NotBlank(message = "Notas são obrigatórias")
        String notes
) {}
