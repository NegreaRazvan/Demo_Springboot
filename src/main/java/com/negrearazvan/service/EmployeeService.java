package com.negrearazvan.service;

import com.negrearazvan.model.Employee;
import com.negrearazvan.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import java.util.List;

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
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    public Employee createEmployee(Employee employee) {
        List<Employee> existingEmployees = employeeRepository.findAll();
        for (Employee existingEmployee : existingEmployees) {
            if (existingEmployee.getEmail().equals(employee.getEmail())) {
                throw new RuntimeException("Employee with email " + employee.getEmail() + " already exists.");
            }
        }
        return employeeRepository.save(employee);
    }

    public Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found with email: " + email));
    }

}