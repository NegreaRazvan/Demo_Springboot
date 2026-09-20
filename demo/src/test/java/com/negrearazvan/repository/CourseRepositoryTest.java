package com.negrearazvan.repository;

import com.negrearazvan.model.Course;
import com.negrearazvan.service.specification.CourseSpecifications;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Test
    void save_persistsAndAssignsId() {
        Course saved = courseRepository.save(new Course("Java", "desc", 40L));

        assertThat(saved.getId()).isNotNull();
        assertThat(courseRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void findAllWithSpecification_filtersByTitlePrefixCaseInsensitive() {
        courseRepository.save(new Course("Java Fundamentals", "desc", 40L));
        courseRepository.save(new Course("JavaScript Basics", "desc", 20L));
        courseRepository.save(new Course("Python Basics", "desc", 20L));

        Specification<Course> spec = CourseSpecifications.titleStart("java");
        Page<Course> result = courseRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(Course::getTitle)
                .containsExactlyInAnyOrder("Java Fundamentals", "JavaScript Basics");
    }

    @Test
    void findAllWithSpecification_filtersByDurationRange() {
        courseRepository.save(new Course("Short", "desc", 10L));
        courseRepository.save(new Course("Medium", "desc", 30L));
        courseRepository.save(new Course("Long", "desc", 60L));

        Specification<Course> spec = CourseSpecifications.rangeDuration(20, 40);
        Page<Course> result = courseRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(Course::getTitle)
                .containsExactly("Medium");
    }

    @Test
    void findAllWithSpecification_returnsEmptyPageWhenNothingMatches() {
        courseRepository.save(new Course("Java", "desc", 40L));

        Specification<Course> spec = CourseSpecifications.titleStart("nope");
        Page<Course> result = courseRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}
