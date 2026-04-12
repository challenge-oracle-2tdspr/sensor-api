package com.sensor.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "sensor.exchange";

    public static final String READING_QUEUE   = "sensor.reading.queue";
    public static final String READING_ROUTING = "sensor.reading";

    @Bean
    public TopicExchange sensorExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue sensorReadingQueue() {
        return QueueBuilder.durable(READING_QUEUE).build();
    }

    @Bean
    public Binding sensorReadingBinding() {
        return BindingBuilder.bind(sensorReadingQueue())
                .to(sensorExchange())
                .with(READING_ROUTING);
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public ApplicationRunner rabbitConnectionCheck(RabbitTemplate rabbitTemplate) {
        return args -> rabbitTemplate.execute(channel -> {
            log.info("RabbitMQ connected: {}", channel.getConnection().getAddress());
            return null;
        });
    }
}