FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests -B

RUN mkdir -p target/extracted && java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="sensor-team" description="Sensor API - Spring Boot Production" version="1.0"

RUN apk update --no-cache && apk upgrade --no-cache && apk add --no-cache curl wget ca-certificates tzdata && rm -rf /var/cache/apk/*

ENV TZ=America/Sao_Paulo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime &&     echo $TZ > /etc/timezone

RUN addgroup -S sensor &&     adduser -S sensor -G sensor

WORKDIR /opt/sensor

COPY --from=builder /build/target/extracted/dependencies/ ./
COPY --from=builder /build/target/extracted/spring-boot-loader/ ./
COPY --from=builder /build/target/extracted/snapshot-dependencies/ ./
COPY --from=builder /build/target/extracted/application/ ./

RUN chown -R sensor:sensor /opt/sensor &&     chmod 500 /opt/sensor

USER sensor

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 CMD wget --quiet --tries=1 --spider http://localhost:${SERVER_PORT:-8081}/actuator/health || exit 1

EXPOSE 8081

ENTRYPOINT ["java",
            "-XX:InitialRAMPercentage=70",
            "-XX:MaxRAMPercentage=70",
            "-XX:+UseG1GC",
            "-XX:+UseStringDeduplication",
            "-XX:+OptimizeStringConcat",
            "-Djava.security.egd=file:/dev/./urandom",
            "-Dspring.profiles.active=prod",
            "-cp",
            "BOOT-INF/classes:BOOT-INF/lib/*",
            "org.springframework.boot.loader.launch.JarLauncher"]