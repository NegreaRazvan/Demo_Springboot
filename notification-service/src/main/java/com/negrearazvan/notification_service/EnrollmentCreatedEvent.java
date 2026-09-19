
package com.negrearazvan.notification_service;

import java.time.Instant;

public record EnrollmentCreatedEvent (
        String eventId,
        Long enrollmentId,
        Long employeeId,
        String employeeEmail,
        Long courseId,
        String courseTitle,
        Instant occurredAt
) {}

