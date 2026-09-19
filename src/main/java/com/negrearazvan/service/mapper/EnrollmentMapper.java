package com.negrearazvan.service.mapper;

import com.negrearazvan.model.Enrollment;
import com.negrearazvan.model.dto.EnrollmentRequest;
import com.negrearazvan.model.dto.EnrollmentResponse;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {

    public EnrollmentResponse toResponse(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getEmployee().getId(),
                enrollment.getCourse().getId(),
                enrollment.getStatus().name(),
                enrollment.getEmployee().getEmail()
        );
    }
}
