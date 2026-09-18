package com.negrearazvan.service;

import com.negrearazvan.model.Course;
import com.negrearazvan.model.Employee;
import com.negrearazvan.model.Enrollment;
import com.negrearazvan.model.dto.EnrollmentRequest;
import com.negrearazvan.model.dto.EnrollmentResponse;
import com.negrearazvan.model.enumeration.EnrollmentStatus;
import com.negrearazvan.model.exception.AlreadyEnrolledException;
import com.negrearazvan.model.exception.ResourceNotFoundException;
import com.negrearazvan.repository.CourseRepository;
import com.negrearazvan.repository.EmployeeRepository;
import com.negrearazvan.repository.EnrollmentRepository;
import com.negrearazvan.service.mapper.EnrollmentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final EmployeeRepository employeeRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentMapper enrollmentMapper;

    public EnrollmentService(EnrollmentRepository enrollmentRepository, EmployeeRepository employeeRepository, CourseRepository courseRepository, EnrollmentMapper enrollmentMapper) {
        this.enrollmentRepository = enrollmentRepository;
        this.employeeRepository = employeeRepository;
        this.courseRepository = courseRepository;
        this.enrollmentMapper = enrollmentMapper;
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getAllEnrollments() {
        return enrollmentRepository.findAll()
                .stream()
                .map(enrollmentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EnrollmentResponse getEnrollmentById(Long id) {
        return enrollmentMapper.toResponse(enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "enrollment")));
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByEmployee(Long employeeId) {
        if (!employeeRepository.existsById(employeeId))
            throw new ResourceNotFoundException(employeeId, "employee");

        return enrollmentRepository.findByEmployeeId(employeeId).stream()
                .map(enrollmentMapper::toResponse)
                .toList();
    }

    @Transactional
    public EnrollmentResponse createEnrollment(EnrollmentRequest request) {
        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException(request.employeeId(), "employee"));
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new ResourceNotFoundException(request.courseId(), "course"));

        if (enrollmentRepository.existsByEmployeeIdAndCourseId(employee.getId(), course.getId())) {
            throw new AlreadyEnrolledException(employee.getId(), course.getId());
        }

        Enrollment enrollment = new Enrollment(employee, course);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        return enrollmentMapper.toResponse(savedEnrollment);
    }

    @Transactional
    public EnrollmentResponse updateStatus(Long enrollmentId, EnrollmentStatus status) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException(enrollmentId, "enrollment"));

        enrollment.setStatus(status);
        return enrollmentMapper.toResponse(enrollment);
    }

}
