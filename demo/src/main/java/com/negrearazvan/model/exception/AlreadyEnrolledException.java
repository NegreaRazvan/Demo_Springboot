package com.negrearazvan.model.exception;

public class AlreadyEnrolledException extends IllegalArgumentException {
    public AlreadyEnrolledException(Long employeeId, Long courseId) {
        super("Employee with id " + employeeId + " is already enrolled in course with id " + courseId);
    }
}
