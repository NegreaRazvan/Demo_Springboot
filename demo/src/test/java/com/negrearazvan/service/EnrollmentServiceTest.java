package com.negrearazvan.service;

import com.negrearazvan.messaging.rabbit.RabbitEnrollmentEventPublisher;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private EnrollmentMapper enrollmentMapper;
    @Mock
    private RabbitEnrollmentEventPublisher enrollmentEventPublisher;

    @InjectMocks
    private EnrollmentService enrollmentService;

    @Test
    void getAllEnrollments() {
        Enrollment enrollment1 = new Enrollment(new Employee("Ana", "Pop", "ana@corp.com", "Eng"), new Course("Java", "desc", 40L));
        Enrollment enrollment2 = new Enrollment(new Employee("John", "Doe", "john@corp", "Eng"), new Course("Python", "desc", 30L));

        when(enrollmentRepository.findAll()).thenReturn(List.of(enrollment1, enrollment2));

        EnrollmentResponse response1 = new EnrollmentResponse(1L, null, null, "ENROLLED", "ana@corp.com");
        EnrollmentResponse response2 = new EnrollmentResponse(2L, null, null, "ENROLLED", "john@corp");
        when(enrollmentMapper.toResponse(enrollment1)).thenReturn(response1);
        when(enrollmentMapper.toResponse(enrollment2)).thenReturn(response2);

        List<EnrollmentResponse> result = enrollmentService.getAllEnrollments();

        assertThat(result).containsExactly(response1, response2);
    }

    @Test
    void getEnrollmentById_throwsWhenNotFound() {
        when(enrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.getEnrollmentById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("enrollment");

        verifyNoInteractions(enrollmentMapper);
    }

    @Test
    void getEnrollmentById_returnsEnrollmentResponseWhenFound() {
        Enrollment enrollment = new Enrollment(new Employee("Ana", "Pop", "ana@corp.com", "Eng"), new Course("Java", "desc", 40L));
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));

        EnrollmentResponse expectedResponse = new EnrollmentResponse(1L, null, null, "ENROLLED", "ana@corp.com");
        when(enrollmentMapper.toResponse(enrollment)).thenReturn(expectedResponse);

        EnrollmentResponse result = enrollmentService.getEnrollmentById(1L);
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getEnrollmentsByEmployee_throwsWhenEmployeeDoesNotExist() {
        when(employeeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> enrollmentService.getEnrollmentsByEmployee(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("employee");

        verifyNoInteractions(enrollmentRepository);
        verifyNoInteractions(enrollmentMapper);
    }

    @Test
    void getEnrollmentsByEmployee_returnsEnrollmentResponsesWhenEmployeeExists() {
        Employee employee = new Employee("Ana", "Pop", "ana@corp.com", "Eng");
        when(employeeRepository.existsById(1L)).thenReturn(true);
        Enrollment enrollment1 = new Enrollment(employee, new Course("Java", "desc", 40L));
        Enrollment enrollment2 = new Enrollment(employee, new Course("Python", "desc", 30L));
        when(enrollmentRepository.findByEmployeeId(1L)).thenReturn(List.of(enrollment1, enrollment2));

        EnrollmentResponse response1 = new EnrollmentResponse(1L, null, null, "ENROLLED", "ana@corp.com");
        EnrollmentResponse response2 = new EnrollmentResponse(2L, null, null, "ENROLLED", "ana@corp.com");
        when(enrollmentMapper.toResponse(enrollment1)).thenReturn(response1);
        when(enrollmentMapper.toResponse(enrollment2)).thenReturn(response2);

        List<EnrollmentResponse> result = enrollmentService.getEnrollmentsByEmployee(1L);
        assertThat(result).containsExactly(response1, response2);
    }

    @Test
    void updateEnrollmentStatus_throwsWhenEnrollmentDoesNotExist() {
        when(enrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.updateStatus(99L, EnrollmentStatus.COMPLETED))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("enrollment");

        verifyNoInteractions(enrollmentMapper);
        verify(enrollmentRepository, never()).update(any());
    }

    @Test
    void updateEnrollmentStatus_updatesStatusWhenEnrollmentExists() {
        Enrollment enrollment = new Enrollment(new Employee("Ana", "Pop", "ana@corp.com", "Eng"), new Course("Java", "desc", 40L));
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));

        EnrollmentResponse expectedResponse = new EnrollmentResponse(1L, null, null, "COMPLETED", "ana@corp.com");
        when(enrollmentMapper.toResponse(enrollment)).thenReturn(expectedResponse);

        EnrollmentResponse result = enrollmentService.updateStatus(1L, EnrollmentStatus.COMPLETED);
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.COMPLETED);
    }

    @Test
    void createEnrollment_throwsWhenEmployeeDoesNotExist() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.createEnrollment(new EnrollmentRequest(99L, 1L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("employee");

        verifyNoInteractions(courseRepository);
        verify(enrollmentRepository, never()).save(any());
        verify(enrollmentEventPublisher, never()).enrollmentCreated(any());
    }

    @Test
    void createEnrollment_throwsWhenCourseDoesNotExist() {
        Employee employee = new Employee("Ana", "Pop", "ana@corp.com", "Eng");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.createEnrollment(new EnrollmentRequest(1L, 99L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("course");

        verify(enrollmentRepository, never()).save(any());
        verify(enrollmentEventPublisher, never()).enrollmentCreated(any());
    }

    @Test
    void createEnrollment_throwsWhenAlreadyEnrolled() {
        Employee employee = new Employee("Ana", "Pop", "ana@corp.com", "Eng");
        Course course = new Course("Java", "desc", 40L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByEmployeeIdAndCourseId(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> enrollmentService.createEnrollment(new EnrollmentRequest(1L, 1L)))
                .isInstanceOf(AlreadyEnrolledException.class);

        verify(enrollmentRepository, never()).save(any());
        verify(enrollmentEventPublisher, never()).enrollmentCreated(any());
    }

    @Test
    void createEnrollment_savesAndPublishesEventOnHappyPath() {
        Employee employee = new Employee("Ana", "Pop", "ana@corp.com", "Eng");
        Course course = new Course("Java", "desc", 40L);
        Enrollment savedEnrollment = new Enrollment(employee, course);
        EnrollmentResponse expectedResponse = new EnrollmentResponse(1L, null, null, "ENROLLED", "ana@corp.com");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByEmployeeIdAndCourseId(any(), any())).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(savedEnrollment);
        when(enrollmentMapper.toResponse(savedEnrollment)).thenReturn(expectedResponse);

        EnrollmentResponse result = enrollmentService.createEnrollment(new EnrollmentRequest(1L, 1L));

        assertThat(result).isEqualTo(expectedResponse);


        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository).save(captor.capture());
        assertThat(captor.getValue().getEmployee()).isEqualTo(employee);
        assertThat(captor.getValue().getCourse()).isEqualTo(course);

        InOrder inOrder = inOrder(enrollmentRepository, enrollmentEventPublisher);
        inOrder.verify(enrollmentRepository).save(any());
        inOrder.verify(enrollmentEventPublisher).enrollmentCreated(savedEnrollment);
    }
}
