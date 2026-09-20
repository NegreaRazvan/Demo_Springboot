package com.negrearazvan.controller;

import com.negrearazvan.model.dto.CourseRequest;
import com.negrearazvan.model.dto.CourseResponse;
import com.negrearazvan.model.dto.PageResponse;
import com.negrearazvan.model.exception.ResourceNotFoundException;
import com.negrearazvan.service.CourseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
public class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CourseService courseService;

    @Test
    void getAllCourses_returnsPageResponseAsJson() throws Exception {
        CourseResponse course = new CourseResponse(1L, "Java", "desc", 40L);
        PageResponse<CourseResponse> page = new PageResponse<>(List.of(course), 0, 20, 1, 1, true);

        when(courseService.search(any(), any())).thenReturn(page);

        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Java"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void getCourseById_returnsCourseWhenFound() throws Exception {
        CourseResponse course = new CourseResponse(1L, "Java", "desc", 40L);
        when(courseService.getCourseById(1L)).thenReturn(course);

        mockMvc.perform(get("/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Java"));
    }

    @Test
    void getCourseById_returns404WhenNotFound() throws Exception {
        when(courseService.getCourseById(99L)).thenThrow(new ResourceNotFoundException(99L, "course"));

        mockMvc.perform(get("/courses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    @Test
    void createCourse_returnsMappedResponse() throws Exception {
        CourseRequest request = new CourseRequest("Java", "desc", 40L);
        CourseResponse response = new CourseResponse(1L, "Java", "desc", 40L);
        when(courseService.createCourse(request)).thenReturn(response);

        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Java"));
    }

    @Test
    void createCourse_returns400WhenValidationFails() throws Exception {
        CourseRequest invalidRequest = new CourseRequest("", "desc", -5L);

        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.duration").exists());

        verifyNoInteractions(courseService);
    }
}
