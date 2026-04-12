package com.sensor.api.service;

import com.sensor.api.domain.Sensor;
import com.sensor.api.dto.SensorCreateRequestDTO;
import com.sensor.api.dto.SensorResponseDTO;
import com.sensor.api.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorService {

    private final SensorRepository sensorRepository;

    @Transactional
    public SensorResponseDTO create(SensorCreateRequestDTO dto) {
        if (sensorRepository.existsById(dto.id())) {
            log.warn("Sensor with id {} already exists, skipping creation.", dto.id());
            return SensorResponseDTO.fromSensor(sensorRepository.getReferenceById(dto.id()));
        }
        if (sensorRepository.existsBySensorCode(dto.sensorCode())) {
            throw new IllegalArgumentException("Sensor code already exists: " + dto.sensorCode());
        }

        Sensor sensor = Sensor.builder()
                .id(dto.id())
                .sensorCode(dto.sensorCode())
                .model(dto.model())
                .manufacturer(dto.manufacturer())
                .installationDate(dto.installationDate())
                .status(dto.status())
                .batteryLevel(dto.batteryLevel())
                .lastMaintenance(dto.lastMaintenance())
                .fieldId(dto.fieldId())
                .createdAt(dto.createdAt())
                .updatedAt(dto.updatedAt())
                .build();

        Sensor saved = sensorRepository.save(sensor);
        log.info("Sensor created: {} (field={})", saved.getSensorCode(), saved.getFieldId());
        return SensorResponseDTO.fromSensor(saved);
    }

    @Transactional(readOnly = true)
    public SensorResponseDTO findById(UUID id) {
        return sensorRepository.findById(id)
                .map(SensorResponseDTO::fromSensor)
                .orElseThrow(() -> new RuntimeException("Sensor not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<SensorResponseDTO> findAll() {
        return sensorRepository.findAll()
                .stream()
                .map(SensorResponseDTO::fromSensor)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Sensor> findAllActive() {
        return sensorRepository.findByStatus("ACTIVE");
    }
}