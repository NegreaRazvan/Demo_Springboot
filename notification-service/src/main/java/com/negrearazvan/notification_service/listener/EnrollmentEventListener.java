package com.negrearazvan.notification_service.listener;

import com.negrearazvan.notification_service.config.RabbitConfig;
import com.negrearazvan.notification_service.EnrollmentCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EnrollmentEventListener {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentEventListener.class);

    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void onEnrollmentCreated(EnrollmentCreatedEvent event) {
        if (!processedEventIds.add(event.eventId())) {
            log.warn("Duplicate event ignored: eventId={}", event.eventId());
            return;
        }
        log.info("NOTIFICATION: {} has been enrolled in '{}' (eventId={}, enrollmentId={})",
                event.employeeEmail(), event.courseTitle(), event.eventId(), event.enrollmentId());
    }
}