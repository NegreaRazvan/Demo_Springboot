package com.negrearazvan.integration;

import com.negrearazvan.model.dto.EmployeeRequest;
import com.negrearazvan.repository.EmployeeRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class EmployeeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Test
    void createThenGetById_roundTripsThroughRealDatabase() throws Exception {
        EmployeeRequest request = new EmployeeRequest("Ana", "Pop", "ana@corp.com", "Eng");

        String createResponse = mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana@corp.com"))
                .andReturn().getResponse().getContentAsString();

        Long createdId = objectMapper.readTree(createResponse).get("id").asLong();
        assertThat(employeeRepository.findById(createdId)).isPresent();

        mockMvc.perform(get("/employees/" + createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Pop"));
    }

    @Test
    void createEmployee_returns409WhenEmailAlreadyRegistered() throws Exception {
        EmployeeRequest request = new EmployeeRequest("Ana", "Pop", "ana@corp.com", "Eng");

        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        EmployeeRequest duplicate = new EmployeeRequest("Alt", "Nume", "ana@corp.com", "Sales");
        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Email already in use"));

        assertThat(employeeRepository.count()).isEqualTo(1);
    }

    @Test
    void getEmployees_filtersByDepartmentAgainstRealData() throws Exception {
        mockMvc.perform(post("/employees").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new EmployeeRequest("Ana", "Pop", "ana@corp.com", "Eng"))));
        mockMvc.perform(post("/employees").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new EmployeeRequest("Ion", "Ionescu", "ion@corp.com", "Sales"))));

        mockMvc.perform(get("/employees").param("department", "eng"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.content[0].firstName").value("Ana"));
    }
}
