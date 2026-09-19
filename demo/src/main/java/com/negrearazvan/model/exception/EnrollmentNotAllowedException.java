package com.negrearazvan.model.exception;

public class EnrollmentNotAllowedException extends RuntimeException {
    public EnrollmentNotAllowedException(Long employeeId, Long courseId) {
        super("Enrollment not allowed for employee with ID " + employeeId + " in course with ID " + courseId);
    }
}
