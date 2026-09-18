package com.negrearazvan.controller;

import com.negrearazvan.model.dto.EnrollmentRequest;
import com.negrearazvan.model.dto.EnrollmentResponse;
import com.negrearazvan.model.dto.StatusUpdateRequest;
import com.negrearazvan.model.enumeration.EnrollmentStatus;
import com.negrearazvan.service.EnrollmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public List<EnrollmentResponse> getAllEnrollments(@RequestParam(required = false) Long employeeId) {
        if (employeeId != null)
            return enrollmentService.getEnrollmentsByEmployee(employeeId);

        return enrollmentService.getAllEnrollments();
    }

    @GetMapping("/{id}")
    public EnrollmentResponse getEnrollmentById(@PathVariable Long id) {
        return enrollmentService.getEnrollmentById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse createEnrollment(@Valid @RequestBody EnrollmentRequest request) {
        return enrollmentService.createEnrollment(request);
    }

    @PatchMapping("/{id}/status")
    public EnrollmentResponse updateStatus(@PathVariable Long id,
                                           @Valid @RequestBody StatusUpdateRequest request) {
        return enrollmentService.updateStatus(id, request.status());
    }

}
