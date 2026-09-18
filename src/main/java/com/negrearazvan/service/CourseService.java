package com.negrearazvan.service;

import com.negrearazvan.model.Course;
import com.negrearazvan.model.dto.CourseRequest;
import com.negrearazvan.model.dto.CourseResponse;
import com.negrearazvan.model.exception.ResourceNotFoundException;
import com.negrearazvan.repository.CourseRepository;
import com.negrearazvan.service.mapper.CourseMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    public CourseService(CourseRepository courseRepository, CourseMapper courseMapper) {
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
    }

    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "course"));
        return courseMapper.toResponse(course);
    }

    public CourseResponse createCourse(CourseRequest course) {
        return courseMapper.toResponse(courseRepository.save(courseMapper.toEntity(course)));
    }

}