package com.negrearazvan.repository;

import com.negrearazvan.model.Enrollment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long>, JpaSpecificationExecutor<Enrollment> {
    @Override
    @EntityGraph(attributePaths = {"employee", "course"})
    public List<Enrollment> findAll();

    public List<Enrollment> findByEmployeeId(Long employeeId);

    public boolean existsByEmployeeIdAndCourseId(Long employeeId, Long courseId);
}
