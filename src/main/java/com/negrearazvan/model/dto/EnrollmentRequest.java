package com.negrearazvan.model.dto;

import com.negrearazvan.model.Course;
import com.negrearazvan.model.Employee;
import com.negrearazvan.model.enumeration.EnrollmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record EnrollmentRequest(
        @NotNull(message = "Employee id is required")
        Long employeeId,

        @NotNull(message = "Course id is required")
        Long courseId
) {}
