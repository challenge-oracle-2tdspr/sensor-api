package com.sensor.api.messaging;

import com.sensor.api.config.RabbitMQConfig;
import com.sensor.api.dto.SensorReadingMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensorReadingProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publish(SensorReadingMessageDTO dto) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.READING_ROUTING,
                dto
        );
        log.debug("Reading published — sensor={} field={}", dto.sensorId(), dto.fieldId());
    }
}