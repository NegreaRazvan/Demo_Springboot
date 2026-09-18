package com.negrearazvan.service;

import com.negrearazvan.model.Course;
import com.negrearazvan.model.dto.CourseFilter;
import com.negrearazvan.model.dto.CourseRequest;
import com.negrearazvan.model.dto.CourseResponse;
import com.negrearazvan.model.dto.PageResponse;
import com.negrearazvan.model.exception.ResourceNotFoundException;
import com.negrearazvan.repository.CourseRepository;
import com.negrearazvan.service.mapper.CourseMapper;
import com.negrearazvan.service.specification.CourseSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    public PageResponse<CourseResponse> search(CourseFilter filter, Pageable pageable){
        Specification<Course> specification = Specification
                .allOf(
                        CourseSpecifications.titleStart(filter.title()),
                        CourseSpecifications.rangeDuration(filter.minDuration(), filter.maxDuration())
                );

        Page<CourseResponse> page = courseRepository.findAll(specification, pageable)
                .map(courseMapper::toResponse);

        return PageResponse.fromPageToPageResponse(page);
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