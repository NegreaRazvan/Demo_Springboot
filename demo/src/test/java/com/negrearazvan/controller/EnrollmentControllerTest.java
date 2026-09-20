package com.negrearazvan.controller;

import com.negrearazvan.model.dto.EnrollmentRequest;
import com.negrearazvan.model.dto.EnrollmentResponse;
import com.negrearazvan.model.dto.StatusUpdateRequest;
import com.negrearazvan.model.enumeration.EnrollmentStatus;
import com.negrearazvan.model.exception.AlreadyEnrolledException;
import com.negrearazvan.model.exception.ResourceNotFoundException;
import com.negrearazvan.service.EnrollmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnrollmentController.class)
public class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EnrollmentService enrollmentService;

    @Test
    void getAllEnrollments_returnsAllWhenNoEmployeeIdGiven() throws Exception {
        EnrollmentResponse enrollment = new EnrollmentResponse(1L, 1L, 2L, "ENROLLED", "ana@corp.com");
        when(enrollmentService.getAllEnrollments()).thenReturn(List.of(enrollment));

        mockMvc.perform(get("/enrollments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ENROLLED"));

        verify(enrollmentService, never()).getEnrollmentsByEmployee(any());
    }

    @Test
    void getAllEnrollments_filtersByEmployeeIdWhenGiven() throws Exception {
        EnrollmentResponse enrollment = new EnrollmentResponse(1L, 1L, 2L, "ENROLLED", "ana@corp.com");
        when(enrollmentService.getEnrollmentsByEmployee(1L)).thenReturn(List.of(enrollment));

        mockMvc.perform(get("/enrollments").param("employeeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeId").value(1));

        verify(enrollmentService, never()).getAllEnrollments();
    }

    @Test
    void getEnrollmentById_returnsEnrollmentWhenFound() throws Exception {
        EnrollmentResponse enrollment = new EnrollmentResponse(1L, 1L, 2L, "ENROLLED", "ana@corp.com");
        when(enrollmentService.getEnrollmentById(1L)).thenReturn(enrollment);

        mockMvc.perform(get("/enrollments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getEnrollmentById_returns404WhenNotFound() throws Exception {
        when(enrollmentService.getEnrollmentById(99L)).thenThrow(new ResourceNotFoundException(99L, "enrollment"));

        mockMvc.perform(get("/enrollments/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    @Test
    void createEnrollment_returns201WithMappedResponse() throws Exception {
        EnrollmentRequest request = new EnrollmentRequest(1L, 2L);
        EnrollmentResponse response = new EnrollmentResponse(1L, 1L, 2L, "ENROLLED", "ana@corp.com");
        when(enrollmentService.createEnrollment(request)).thenReturn(response);

        mockMvc.perform(post("/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ENROLLED"));
    }

    @Test
    void createEnrollment_returns400WhenValidationFails() throws Exception {
        EnrollmentRequest invalidRequest = new EnrollmentRequest(null, null);

        mockMvc.perform(post("/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.employeeId").exists())
                .andExpect(jsonPath("$.errors.courseId").exists());

        verifyNoInteractions(enrollmentService);
    }

    @Test
    void createEnrollment_returns409WhenAlreadyEnrolled() throws Exception {
        EnrollmentRequest request = new EnrollmentRequest(1L, 2L);
        when(enrollmentService.createEnrollment(request)).thenThrow(new AlreadyEnrolledException(1L, 2L));

        mockMvc.perform(post("/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Already enrolled"));
    }

    @Test
    void updateStatus_returnsUpdatedEnrollment() throws Exception {
        StatusUpdateRequest request = new StatusUpdateRequest(EnrollmentStatus.COMPLETED);
        EnrollmentResponse response = new EnrollmentResponse(1L, 1L, 2L, "COMPLETED", "ana@corp.com");
        when(enrollmentService.updateStatus(1L, EnrollmentStatus.COMPLETED)).thenReturn(response);

        mockMvc.perform(patch("/enrollments/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void updateStatus_returns400WhenStatusMissing() throws Exception {
        StatusUpdateRequest invalidRequest = new StatusUpdateRequest(null);

        mockMvc.perform(patch("/enrollments/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.status").exists());

        verifyNoInteractions(enrollmentService);
    }
}
