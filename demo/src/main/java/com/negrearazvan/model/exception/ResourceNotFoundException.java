package com.negrearazvan.model.exception;


public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(Long id, String resourceName) {
        super(String.format("%s with id '%d' not found", resourceName, id));
    }

    public ResourceNotFoundException(String resourceName, String fieldName, String fieldValue) {
        super(String.format("%s with %s '%s' not found", resourceName, fieldName, fieldValue));
    }
}