package com.sensor.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SensorReadingMessageDTO(
        UUID sensorId,
        UUID fieldId,
        LocalDateTime readingTime,
        BigDecimal temperature,
        BigDecimal humidity,
        BigDecimal soilMoisture,
        BigDecimal windSpeed,
        BigDecimal windDirection,
        BigDecimal rainfall,
        BigDecimal soilPh,
        BigDecimal lightIntensity
) {}