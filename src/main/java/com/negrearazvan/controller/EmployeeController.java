package com.negrearazvan.controller;


import com.negrearazvan.model.Employee;
import com.negrearazvan.model.dto.EmployeeRequest;
import com.negrearazvan.model.dto.EmployeeResponse;
import com.negrearazvan.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<EmployeeResponse> getEmployees(@RequestParam(required = false) String email) {
        if (email != null && !email.isBlank())
            return employeeService.getEmployeeByEmail(email);
        return employeeService.getAllEmployees();
    }

    @GetMapping("/{id}")
    public EmployeeResponse getEmployeeById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse createEmployee(@Valid @RequestBody EmployeeRequest employee) {
        return employeeService.createEmployee(employee);
    }
}