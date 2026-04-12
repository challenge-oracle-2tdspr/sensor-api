package com.sensor.api.dto;

import com.sensor.api.domain.Sensor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record SensorResponseDTO(
        UUID id,
        String sensorCode,
        String model,
        String manufacturer,
        LocalDateTime installationDate,
        String status,
        Integer batteryLevel,
        LocalDateTime lastMaintenance,
        UUID fieldId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static SensorResponseDTO fromSensor(Sensor sensor) {
        return SensorResponseDTO.builder()
                .id(sensor.getId())
                .sensorCode(sensor.getSensorCode())
                .model(sensor.getModel())
                .manufacturer(sensor.getManufacturer())
                .installationDate(sensor.getInstallationDate())
                .status(sensor.getStatus())
                .batteryLevel(sensor.getBatteryLevel())
                .lastMaintenance(sensor.getLastMaintenance())
                .fieldId(sensor.getFieldId())
                .createdAt(sensor.getCreatedAt())
                .updatedAt(sensor.getUpdatedAt())
                .build();
    }
}