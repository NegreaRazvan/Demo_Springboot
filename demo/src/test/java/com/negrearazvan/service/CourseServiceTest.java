package com.negrearazvan.service;

import com.negrearazvan.model.Course;
import com.negrearazvan.model.dto.CourseFilter;
import com.negrearazvan.model.dto.CourseResponse;
import com.negrearazvan.model.dto.PageResponse;
import com.negrearazvan.model.dto.CourseRequest;
import com.negrearazvan.model.exception.ResourceNotFoundException;
import com.negrearazvan.repository.CourseRepository;
import com.negrearazvan.service.mapper.CourseMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CourseMapper courseMapper;

    @InjectMocks
    private CourseService courseService;

    @Test
    void testGetAllCourses() {
        Course course1 = new Course("Java", "desc", 40L);
        Course course2 = new Course("Python", "desc", 30L);

        when(courseRepository.findAll()).thenReturn(List.of(course1, course2));

        CourseResponse response1 = new CourseResponse(1L, "Java", "desc", 40L);
        CourseResponse response2 = new CourseResponse(2L, "Python", "desc", 30L);
        when(courseMapper.toResponse(course1)).thenReturn(response1);
        when(courseMapper.toResponse(course2)).thenReturn(response2);

        List<CourseResponse> result = courseService.getAllCourses();

        assertThat(result).containsExactly(response1, response2);
    }

    @Test
    void search_mapsRepositoryPageToPageResponse() {
        Course java = new Course("Java", "desc", 40L);
        Course spring = new Course("Spring", "desc2", 30L);

        Pageable pageable = PageRequest.of(0, 2, Sort.by("title"));
        Page<Course> repositoryPage = new PageImpl<>(List.of(java, spring), pageable, 5);

        when(courseRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(repositoryPage);
        when(courseMapper.toResponse(java)).thenReturn(new CourseResponse(1L, "Java", "desc", 40L));
        when(courseMapper.toResponse(spring)).thenReturn(new CourseResponse(2L, "Spring", "desc2", 30L));

        PageResponse<CourseResponse> result = courseService.search(new CourseFilter(null, null, null), pageable);

        assertThat(result.content())
                .extracting(CourseResponse::title)
                .containsExactly("Java", "Spring");
        assertThat(result.pageNumber()).isEqualTo(0);
        assertThat(result.pageSize()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(5);
        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.last()).isFalse();
    }

    @Test
    void search_returnsEmptyPageResponseWhenNoMatches() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("title"));
        Page<Course> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(courseRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        PageResponse<CourseResponse> result = courseService.search(new CourseFilter("nope", null, null), pageable);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
        assertThat(result.totalPages()).isZero();
        assertThat(result.last()).isTrue();
    }

    @Test
    void search_forwardsThePageableItReceivedUnchanged() {
        Pageable pageable = PageRequest.of(2, 5, Sort.by(Sort.Direction.DESC, "duration"));
        when(courseRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

        courseService.search(new CourseFilter(null, null, null), pageable);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        org.mockito.Mockito.verify(courseRepository).findAll(any(Specification.class), pageableCaptor.capture());

        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(5);
        assertThat(pageableCaptor.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "duration"));
    }

    @Test
    void getCourseById_returnsMappedResponseWhenFound() {
        Course course = new Course("Java", "desc", 40L);
        CourseResponse response = new CourseResponse(1L, "Java", "desc", 40L);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseMapper.toResponse(course)).thenReturn(response);

        CourseResponse result = courseService.getCourseById(1L);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void getCourseById_throwsWhenNotFound() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("course");

        verifyNoInteractions(courseMapper);
    }

    @Test
    void testCreateCourse() {
        CourseRequest request = new CourseRequest("Java", "desc", 40L);
        Course entity = new Course("Java", "desc", 40L);
        Course savedEntity = new Course("Java", "desc", 40L);
        CourseResponse response = new CourseResponse(1L, "Java", "desc", 40L);

        when(courseMapper.toEntity(request)).thenReturn(entity);
        when(courseRepository.save(entity)).thenReturn(savedEntity);
        when(courseMapper.toResponse(savedEntity)).thenReturn(response);

        CourseResponse result = courseService.createCourse(request);

        assertThat(result).isEqualTo(response);
        verify(courseRepository).save(entity);
    }
}
