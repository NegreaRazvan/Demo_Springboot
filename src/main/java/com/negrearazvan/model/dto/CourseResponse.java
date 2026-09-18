package com.negrearazvan.model.dto;

public record CourseResponse(
        Long id,
        String title,
        String description,
        Long duration
) {
}
