package com.negrearazvan.service.policy;

import com.negrearazvan.model.Course;
import com.negrearazvan.model.Employee;

@FunctionalInterface
public interface EnrollmentPolicy {
    boolean isEnrollmentAllowed(Employee employee, Course course);

    EnrollmentPolicy ANYONE = (employee, course) -> true;

    EnrollmentPolicy HR_ONLY = (employee, course) -> "HR".equalsIgnoreCase(employee.getDepartment());

    static EnrollmentPolicy maxDuration(int maxDuration) {
        return (employee, course) -> course.getDuration() <= maxDuration;
    }

    default EnrollmentPolicy and(EnrollmentPolicy other) {
        return (employee, course) -> this.isEnrollmentAllowed(employee, course) && other.isEnrollmentAllowed(employee, course);
    }
}
