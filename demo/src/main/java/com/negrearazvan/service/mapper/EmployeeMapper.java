package com.negrearazvan.service.mapper;

import com.negrearazvan.model.Employee;
import com.negrearazvan.model.dto.EmployeeRequest;
import com.negrearazvan.model.dto.EmployeeResponse;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public Employee toEntity(EmployeeRequest request) {
        return new Employee(
                request.firstName(),
                request.lastName(),
                request.email().toLowerCase(),
                request.department()
        );
    }

    public EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getDepartment()
        );
    }
}
