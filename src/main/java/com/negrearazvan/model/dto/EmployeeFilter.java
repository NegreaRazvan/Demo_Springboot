package com.negrearazvan.model.dto;

public record EmployeeFilter(
        String email,
        String department,
        String lastName
) {}
