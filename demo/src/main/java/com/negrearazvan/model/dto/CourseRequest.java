package com.negrearazvan.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CourseRequest (
        @NotBlank(message = "Title is mandatory")
        String title,

        @NotBlank(message = "Description is mandatory")
        String description,

        @Min(value = 1, message = "Duration must be greater than 0")
        @Positive(message = "Duration must be a positive number")
        @NotNull(message = "Duration is mandatory")
        Long duration
) {}
