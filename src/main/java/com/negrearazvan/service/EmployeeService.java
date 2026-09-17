package com.negrearazvan.service;

import com.negrearazvan.model.Employee;
import com.negrearazvan.model.exception.EmailAlreadyExistsException;
import com.negrearazvan.model.exception.ResourceNotFoundException;
import com.negrearazvan.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "employee"));
    }

    @Transactional
    public Employee createEmployee(Employee employee) {
        employeeRepository.findByEmail(employee.getEmail()).ifPresent(existingEmployee -> {
            throw new EmailAlreadyExistsException(employee.getEmail());
        });
        return employeeRepository.save(employee);
    }

    public Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("employee", "email", email));
    }

}