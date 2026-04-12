package com.sensor.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record SensorCreateRequestDTO(

        @NotNull
        UUID id,

        @NotBlank
        String sensorCode,

        String model,

        String manufacturer,

        LocalDateTime installationDate,

        @NotBlank
        String status,

        Integer batteryLevel,

        LocalDateTime lastMaintenance,

        @NotNull
        UUID fieldId,

        @NotNull
        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {}