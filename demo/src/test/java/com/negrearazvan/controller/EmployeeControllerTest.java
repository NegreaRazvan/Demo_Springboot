package com.negrearazvan.controller;

import com.negrearazvan.model.dto.EmployeeRequest;
import com.negrearazvan.model.dto.EmployeeResponse;
import com.negrearazvan.model.dto.PageResponse;
import com.negrearazvan.model.exception.EmailAlreadyExistsException;
import com.negrearazvan.model.exception.ResourceNotFoundException;
import com.negrearazvan.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    void getEmployees_returnsPageResponseAsJson() throws Exception {
        EmployeeResponse employee = new EmployeeResponse(1L, "Ana", "Pop", "ana@corp.com", "Eng");
        PageResponse<EmployeeResponse> page = new PageResponse<>(List.of(employee), 0, 20, 1, 1, true);

        when(employeeService.search(any(), any())).thenReturn(page);

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].lastName").value("Pop"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void getEmployeeById_returnsEmployeeWhenFound() throws Exception {
        EmployeeResponse employee = new EmployeeResponse(1L, "Ana", "Pop", "ana@corp.com", "Eng");
        when(employeeService.getEmployeeById(1L)).thenReturn(employee);

        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ana@corp.com"));
    }

    @Test
    void getEmployeeById_returns404WhenNotFound() throws Exception {
        when(employeeService.getEmployeeById(99L)).thenThrow(new ResourceNotFoundException(99L, "employee"));

        mockMvc.perform(get("/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    @Test
    void createEmployee_returns201WithMappedResponse() throws Exception {
        EmployeeRequest request = new EmployeeRequest("Ana", "Pop", "ana@corp.com", "Eng");
        EmployeeResponse response = new EmployeeResponse(1L, "Ana", "Pop", "ana@corp.com", "Eng");
        when(employeeService.createEmployee(request)).thenReturn(response);

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ana@corp.com"));
    }

    @Test
    void createEmployee_returns400WhenValidationFails() throws Exception {
        EmployeeRequest invalidRequest = new EmployeeRequest("", "Pop", "not-an-email", "Eng");

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.email").exists());

        verifyNoInteractions(employeeService);
    }

    @Test
    void createEmployee_returns409WhenEmailAlreadyExists() throws Exception {
        EmployeeRequest request = new EmployeeRequest("Ana", "Pop", "ana@corp.com", "Eng");
        when(employeeService.createEmployee(request)).thenThrow(new EmailAlreadyExistsException("ana@corp.com"));

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Email already in use"));
    }
}
