package com.negrearazvan.model.dto;

public record CourseFilter(
        String title,
        Integer minDuration,
        Integer maxDuration
) {
}
