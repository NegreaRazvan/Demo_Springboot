package com.negrearazvan.integration;

import com.negrearazvan.config.RabbitConfig;
import com.negrearazvan.model.Course;
import com.negrearazvan.model.Employee;
import com.negrearazvan.model.dto.EnrollmentRequest;
import com.negrearazvan.model.dto.StatusUpdateRequest;
import com.negrearazvan.model.enumeration.EnrollmentStatus;
import com.negrearazvan.repository.CourseRepository;
import com.negrearazvan.repository.EmployeeRepository;
import com.negrearazvan.repository.EnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class EnrollmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CourseRepository courseRepository;

    // real broker isn't running in this environment; this proves the event
    // WOULD have been published, without needing RabbitMQ up
    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    private Employee employee;
    private Course course;

    @BeforeEach
    void setUp() {
        employee = employeeRepository.save(new Employee("Ana", "Pop", "ana@corp.com", "Eng"));
        course = courseRepository.save(new Course("Java", "desc", 40L));
    }

    @Test
    void createEnrollment_persistsAndPublishesEvent() throws Exception {
        EnrollmentRequest request = new EnrollmentRequest(employee.getId(), course.getId());

        mockMvc.perform(post("/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ENROLLED"))
                .andExpect(jsonPath("$.employeeEmail").value("ana@corp.com"));

        assertThat(enrollmentRepository.existsByEmployeeIdAndCourseId(employee.getId(), course.getId())).isTrue();

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitConfig.EXCHANGE),
                eq(RabbitConfig.ROUTING_KEY_ENROLLMENT_CREATED),
                any(Object.class));
    }

    @Test
    void createEnrollment_returns409AndDoesNotPublishWhenAlreadyEnrolled() throws Exception {
        EnrollmentRequest request = new EnrollmentRequest(employee.getId(), course.getId());
        mockMvc.perform(post("/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        verify(rabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class));

        mockMvc.perform(post("/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        // still only the one call from the first, successful request
        verify(rabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class));
        assertThat(enrollmentRepository.count()).isEqualTo(1);
    }

    @Test
    void createEnrollment_returns404AndDoesNotPublishWhenEmployeeMissing() throws Exception {
        EnrollmentRequest request = new EnrollmentRequest(999L, course.getId());

        mockMvc.perform(post("/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verifyNoInteractions(rabbitTemplate);
        assertThat(enrollmentRepository.count()).isZero();
    }

    @Test
    void getAllEnrollments_filtersByEmployeeIdQueryParam() throws Exception {
        mockMvc.perform(post("/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new EnrollmentRequest(employee.getId(), course.getId()))));

        mockMvc.perform(get("/enrollments").param("employeeId", employee.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].employeeId").value(employee.getId()));

        Employee other = employeeRepository.save(new Employee("Ion", "Ionescu", "ion@corp.com", "Sales"));
        mockMvc.perform(get("/enrollments").param("employeeId", other.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }

    @Test
    void updateStatus_persistsNewStatus() throws Exception {
        String createResponse = mockMvc.perform(post("/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EnrollmentRequest(employee.getId(), course.getId()))))
                .andReturn().getResponse().getContentAsString();
        Long enrollmentId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(patch("/enrollments/" + enrollmentId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new StatusUpdateRequest(EnrollmentStatus.COMPLETED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        assertThat(enrollmentRepository.findById(enrollmentId).orElseThrow().getStatus())
                .isEqualTo(EnrollmentStatus.COMPLETED);
    }
}
