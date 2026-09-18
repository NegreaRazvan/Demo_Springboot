package com.negrearazvan.service;

import com.negrearazvan.model.Employee;
import com.negrearazvan.model.dto.EmployeeFilter;
import com.negrearazvan.model.dto.EmployeeRequest;
import com.negrearazvan.model.dto.EmployeeResponse;
import com.negrearazvan.model.dto.PageResponse;
import com.negrearazvan.model.exception.EmailAlreadyExistsException;
import com.negrearazvan.model.exception.ResourceNotFoundException;
import com.negrearazvan.repository.EmployeeRepository;
import com.negrearazvan.service.mapper.EmployeeMapper;
import com.negrearazvan.service.specification.EmployeeSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    public EmployeeService(EmployeeRepository employeeRepository, EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
    }

    //previous method for searching for all employees -> became search so I can do filtering and pagination
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(employeeMapper::toResponse)
                .toList();
    }

    public PageResponse<EmployeeResponse> search(EmployeeFilter filter, Pageable pageable){
        Specification<Employee> specification = Specification
                .allOf(
                        EmployeeSpecifications.hasEmail(filter.email()),
                        EmployeeSpecifications.inDepartment(filter.department()),
                        EmployeeSpecifications.lastNameContains(filter.lastName())
                );

        Page<EmployeeResponse> page = employeeRepository.findAll(specification, pageable)
                .map(employeeMapper::toResponse);

        return PageResponse.fromPageToPageResponse(page);
    }

    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "employee"));
        return employeeMapper.toResponse(employee);
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        employeeRepository.findByEmail(request.email()).ifPresent(existingEmployee -> {
            throw new EmailAlreadyExistsException(request.email());
        });
        Employee saved = employeeRepository.save(employeeMapper.toEntity(request));
        return employeeMapper.toResponse(saved);
    }

    public List<EmployeeResponse> getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email.toLowerCase())
                .map(employeeMapper::toResponse)
                .stream()
                .toList();
    }

}