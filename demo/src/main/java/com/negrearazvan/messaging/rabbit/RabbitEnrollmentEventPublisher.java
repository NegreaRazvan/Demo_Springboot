package com.negrearazvan.messaging.rabbit;

import com.negrearazvan.config.RabbitConfig;
import com.negrearazvan.model.Enrollment;
import com.negrearazvan.model.event.EnrollmentCreatedEvent;
import com.negrearazvan.service.publisher.EnrollmentEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class RabbitEnrollmentEventPublisher implements EnrollmentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitEnrollmentEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitEnrollmentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void enrollmentCreated(Enrollment enrollment) {
        EnrollmentCreatedEvent event = new EnrollmentCreatedEvent(
                UUID.randomUUID().toString(),
                enrollment.getId(),
                enrollment.getEmployee().getId(),
                enrollment.getEmployee().getEmail(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getTitle(),
                Instant.now()
        );

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.ROUTING_KEY_ENROLLMENT_CREATED,
                event
        );

        log.info("Published enrollment.created eventId={} enrollmentId={}",
                event.eventId(), event.enrollmentId());
    }
}
