package com.sensor.api.repository;

import com.sensor.api.domain.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SensorRepository extends JpaRepository<Sensor, UUID> {
    boolean existsBySensorCode(String sensorCode);
    List<Sensor> findByStatus(String status);
}