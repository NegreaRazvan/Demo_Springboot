package com.negrearazvan.controller;


import com.negrearazvan.model.Employee;
import com.negrearazvan.model.dto.EmployeeFilter;
import com.negrearazvan.model.dto.EmployeeRequest;
import com.negrearazvan.model.dto.EmployeeResponse;
import com.negrearazvan.model.dto.PageResponse;
import com.negrearazvan.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    public PageResponse<EmployeeResponse> getEmployees(
            @ModelAttribute EmployeeFilter filter,
            @PageableDefault(sort = "lastName", direction = Sort.Direction.ASC) Pageable pageable) {

        return employeeService.search(filter, pageable);
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