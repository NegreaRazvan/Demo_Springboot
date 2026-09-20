package com.negrearazvan.integration;

import com.negrearazvan.model.dto.CourseRequest;
import com.negrearazvan.repository.CourseRepository;
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
public class CourseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Test
    void createThenGetById_roundTripsThroughRealDatabase() throws Exception {
        CourseRequest request = new CourseRequest("Java", "desc", 40L);

        String createResponse = mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Java"))
                .andReturn().getResponse().getContentAsString();

        Long createdId = objectMapper.readTree(createResponse).get("id").asLong();
        assertThat(courseRepository.findById(createdId)).isPresent();

        mockMvc.perform(get("/courses/" + createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Java"))
                .andExpect(jsonPath("$.duration").value(40));
    }

    @Test
    void getAllCourses_filtersAndPaginatesAgainstRealData() throws Exception {
        mockMvc.perform(post("/courses").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CourseRequest("Java Fundamentals", "desc", 40L))));
        mockMvc.perform(post("/courses").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CourseRequest("Python Basics", "desc", 20L))));

        mockMvc.perform(get("/courses").param("title", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Java Fundamentals"));
    }

    @Test
    void createCourse_returns400WhenValidationFails() throws Exception {
        CourseRequest invalidRequest = new CourseRequest("", "desc", -1L);

        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        assertThat(courseRepository.count()).isZero();
    }
}
