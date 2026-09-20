package com.negrearazvan.repository;

import com.negrearazvan.model.Course;
import com.negrearazvan.model.Employee;
import com.negrearazvan.model.Enrollment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
public class EnrollmentRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Test
    void save_persistsAndAssignsId() {
        Employee employee = em.persistAndFlush(new Employee("Ana", "Pop", "ana@corp.com", "Eng"));
        Course course = em.persistAndFlush(new Course("Java", "desc", 40L));

        Enrollment saved = enrollmentRepository.save(new Enrollment(employee, course));

        assertThat(saved.getId()).isNotNull();
        assertThat(enrollmentRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void findByEmployeeId_returnsOnlyThatEmployeesEnrollments() {
        Employee ana = em.persistAndFlush(new Employee("Ana", "Pop", "ana@corp.com", "Eng"));
        Employee ion = em.persistAndFlush(new Employee("Ion", "Ionescu", "ion@corp.com", "Sales"));
        Course java = em.persistAndFlush(new Course("Java", "desc", 40L));
        Course python = em.persistAndFlush(new Course("Python", "desc", 30L));

        em.persistAndFlush(new Enrollment(ana, java));
        em.persistAndFlush(new Enrollment(ana, python));
        em.persistAndFlush(new Enrollment(ion, java));

        List<Enrollment> result = enrollmentRepository.findByEmployeeId(ana.getId());

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(e -> assertThat(e.getEmployee().getId()).isEqualTo(ana.getId()));
    }

    @Test
    void existsByEmployeeIdAndCourseId_trueWhenEnrollmentExists() {
        Employee employee = em.persistAndFlush(new Employee("Ana", "Pop", "ana@corp.com", "Eng"));
        Course course = em.persistAndFlush(new Course("Java", "desc", 40L));
        em.persistAndFlush(new Enrollment(employee, course));

        boolean exists = enrollmentRepository.existsByEmployeeIdAndCourseId(employee.getId(), course.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmployeeIdAndCourseId_falseWhenNoSuchEnrollment() {
        Employee employee = em.persistAndFlush(new Employee("Ana", "Pop", "ana@corp.com", "Eng"));
        Course course = em.persistAndFlush(new Course("Java", "desc", 40L));

        boolean exists = enrollmentRepository.existsByEmployeeIdAndCourseId(employee.getId(), course.getId());

        assertThat(exists).isFalse();
    }

    @Test
    void save_throwsWhenEmployeeAlreadyEnrolledInSameCourse() {
        Employee employee = em.persistAndFlush(new Employee("Ana", "Pop", "ana@corp.com", "Eng"));
        Course course = em.persistAndFlush(new Course("Java", "desc", 40L));
        em.persistAndFlush(new Enrollment(employee, course));

        Enrollment duplicate = new Enrollment(employee, course);

        assertThatThrownBy(() -> enrollmentRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findAll_loadsEmployeeAndCourseEagerlyViaEntityGraph() {
        Employee employee = em.persistAndFlush(new Employee("Ana", "Pop", "ana@corp.com", "Eng"));
        Course course = em.persistAndFlush(new Course("Java", "desc", 40L));
        em.persistAndFlush(new Enrollment(employee, course));
        em.clear();

        List<Enrollment> result = enrollmentRepository.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmployee().getEmail()).isEqualTo("ana@corp.com");
        assertThat(result.get(0).getCourse().getTitle()).isEqualTo("Java");
    }
}
