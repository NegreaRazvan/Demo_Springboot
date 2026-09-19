package com.negrearazvan.service.publisher;

import com.negrearazvan.model.Enrollment;
import org.springframework.stereotype.Component;

public interface EnrollmentEventPublisher {
    void enrollmentCreated(Enrollment enrollment);
}
