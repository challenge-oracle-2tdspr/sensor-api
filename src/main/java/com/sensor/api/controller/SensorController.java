package com.sensor.api.controller;

import com.sensor.api.dto.SensorCreateRequestDTO;
import com.sensor.api.dto.SensorResponseDTO;
import com.sensor.api.service.SensorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sensors")
public class SensorController {

    private final SensorService sensorService;

    @PostMapping("/create")
    public ResponseEntity<SensorResponseDTO> create(@Valid @RequestBody SensorCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sensorService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SensorResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(sensorService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<SensorResponseDTO>> findAll() {
        return ResponseEntity.ok(sensorService.findAll());
    }
}