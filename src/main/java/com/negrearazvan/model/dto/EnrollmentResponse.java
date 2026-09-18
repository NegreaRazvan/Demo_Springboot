package com.negrearazvan.model.dto;

public record EnrollmentResponse(
        Long id,
        Long employeeId,
        Long courseId,
        String status
) {
}
