package com.negrearazvan.service.mapper;

import com.negrearazvan.model.Course;
import com.negrearazvan.model.dto.CourseRequest;
import com.negrearazvan.model.dto.CourseResponse;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    public Course toEntity(CourseRequest request){
        return new Course(
                request.title(),
                request.description(),
                request.duration()
        );
    }

    public CourseResponse toResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getDuration()
        );
    }
}
