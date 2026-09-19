package com.negrearazvan.model.dto;

import com.negrearazvan.model.enumeration.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull(message = "Status is required")
        EnrollmentStatus status
) {}

