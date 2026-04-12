package com.sensor.api.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sensors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sensor {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String sensorCode;

    private String model;

    private String manufacturer;

    private LocalDateTime installationDate;

    @Column(nullable = false)
    private String status;

    private Integer batteryLevel;

    private LocalDateTime lastMaintenance;

    @Column(nullable = false)
    private UUID fieldId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}