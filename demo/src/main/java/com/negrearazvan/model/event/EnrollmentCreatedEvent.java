package com.negrearazvan.model.event;

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
