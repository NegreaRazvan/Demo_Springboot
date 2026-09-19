package com.negrearazvan.service.specification;

import com.negrearazvan.model.Course;
import org.springframework.data.jpa.domain.Specification;

public final class CourseSpecifications {

    private CourseSpecifications() {}

    public static Specification<Course> titleStart(String title) {
        if (title == null || title.isBlank()) return (root, query, cb) -> cb.conjunction();
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), title.toLowerCase() + "%");
    }

    public static Specification<Course> rangeDuration(Integer minDuration, Integer maxDuration) {
        if (minDuration == null && maxDuration == null) return (root, query, cb) -> cb.conjunction();
        if (minDuration != null && maxDuration != null) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("duration"), minDuration, maxDuration);
        } else if (minDuration != null) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("duration"), minDuration);
        } else {
            return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("duration"), maxDuration);
        }
    }
}
