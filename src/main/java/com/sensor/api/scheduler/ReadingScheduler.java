package com.sensor.api.scheduler;

import com.sensor.api.domain.Sensor;
import com.sensor.api.dto.SensorReadingMessageDTO;
import com.sensor.api.messaging.SensorReadingProducer;
import com.sensor.api.service.SensorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReadingScheduler {

    private final SensorService sensorService;
    private final SensorReadingProducer producer;
    private final Random random = new Random();

    private final Map<UUID, SensorState> states = new ConcurrentHashMap<>();

    @Scheduled(fixedDelay = 10000)
    public void generateReadings() {
        List<Sensor> activeSensors = sensorService.findAllActive();
        if (activeSensors.isEmpty()) {
            log.debug("No active sensors found, skipping.");
            return;
        }
        activeSensors.forEach(sensor -> producer.publish(buildReading(sensor)));
        log.info("[{}] {} reading(s) published.", LocalDateTime.now(), activeSensors.size());
    }

    private SensorReadingMessageDTO buildReading(Sensor sensor) {
        LocalTime now = LocalTime.now();
        SensorState state = states.computeIfAbsent(sensor.getId(), id -> new SensorState(now));

        state.update(now, random);

        return new SensorReadingMessageDTO(
                sensor.getId(),
                sensor.getFieldId(),
                LocalDateTime.now(),
                bd(state.temperature, 2),
                bd(state.humidity, 2),
                bd(state.soilMoisture, 2),
                bd(state.windSpeed, 2),
                bd(state.windDirection,2),
                bd(state.rainfall, 2),
                bd(state.soilPh, 2),
                bd(state.lightIntensity, 2)
        );
    }

    private BigDecimal bd(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
    }

    static class SensorState {
        double temperature;
        double humidity;
        double soilMoisture;
        double windSpeed;
        double windDirection;
        double rainfall;
        double soilPh;
        double lightIntensity;

        SensorState(LocalTime now) {
            temperature = baseTemperature(now);
            humidity = 60 + rand(20);
            soilMoisture = 40 + rand(20);
            windSpeed = 5  + rand(10);
            windDirection = rand(360);
            rainfall = 0;
            soilPh = 6.0 + rand(1.5);
            lightIntensity = baseLightIntensity(now);
        }

        void update(LocalTime now, Random rng) {
            double targetTemp  = baseTemperature(now);
            double targetLight = baseLightIntensity(now);

            temperature = drift(temperature, targetTemp,0.10,0.5, rng,0,70);
            lightIntensity = drift(lightIntensity, targetLight, 0.15, 500, rng,  0,  99999);

            double targetHumidity = 100 - (temperature / 70.0) * 50;
            humidity = drift(humidity, targetHumidity, 0.08, 1.0, rng, 0, 100);
            soilMoisture = drift(soilMoisture,humidity * 0.7, 0.05, 0.5, rng, 0, 100);

            windSpeed     = drift(windSpeed,10, 0.05, 1.5, rng, 0, 999);
            windDirection = wrapAngle(windDirection + gaussSmall(rng, 5));

            int hour = now.getHour();
            if (hour >= 6 && hour <= 18 && rng.nextDouble() < 0.10) {
                rainfall = 0.5 + rng.nextDouble() * 9.5;
            } else {
                rainfall = Math.max(0, rainfall - 0.5);
            }

            soilPh = clamp(soilPh + gaussSmall(rng, 0.02), 0, 14);
        }

        static double baseTemperature(LocalTime t) {
            double hour = t.getHour() + t.getMinute() / 60.0;
            return 18 + 14 * Math.sin(Math.PI * (hour - 5) / 18.0);
        }

        static double baseLightIntensity(LocalTime t) {
            int hour = t.getHour();
            if (hour < 6 || hour >= 19) return 0;
            double progress = (hour + t.getMinute() / 60.0 - 6) / 13.0;
            return 80000 * Math.sin(Math.PI * progress);
        }

        static double drift(double current, double target, double rate, double noise,
                            Random rng, double min, double max) {
            double next = current + (target - current) * rate + gaussSmall(rng, noise);
            return clamp(next, min, max);
        }

        static double gaussSmall(Random rng, double stddev) {
            return rng.nextGaussian() * stddev;
        }

        static double rand(double range) {
            return Math.random() * range;
        }

        static double clamp(double v, double min, double max) {
            return Math.max(min, Math.min(max, v));
        }

        static double wrapAngle(double degrees) {
            return ((degrees % 360) + 360) % 360;
        }
    }
}