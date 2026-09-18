package com.negrearazvan.controller;


import com.negrearazvan.model.Course;
import com.negrearazvan.model.dto.*;
import com.negrearazvan.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public PageResponse<CourseResponse> getAllCourses(
            @ModelAttribute CourseFilter filter,
            @PageableDefault(sort = "title", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return courseService.search(filter, pageable);
    }

    @GetMapping("/{id}")
    public CourseResponse getCourseById(@PathVariable Long id) {
        return courseService.getCourseById(id);
    }

    @PostMapping
    public CourseResponse createCourse(@Valid @RequestBody CourseRequest course) {
        return courseService.createCourse(course);
    }
}