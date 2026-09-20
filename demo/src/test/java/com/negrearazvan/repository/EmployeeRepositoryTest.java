package com.negrearazvan.repository;

import com.negrearazvan.model.Employee;
import com.negrearazvan.service.specification.EmployeeSpecifications;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
public class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void save_persistsAndAssignsId() {
        Employee saved = employeeRepository.save(new Employee("Ana", "Pop", "ana@corp.com", "Eng"));

        assertThat(saved.getId()).isNotNull();
        assertThat(employeeRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void findByEmail_returnsEmployeeWhenPresent() {
        employeeRepository.save(new Employee("Ana", "Pop", "ana@corp.com", "Eng"));

        Optional<Employee> result = employeeRepository.findByEmail("ana@corp.com");

        assertThat(result).isPresent();
        assertThat(result.get().getLastName()).isEqualTo("Pop");
    }

    @Test
    void findByEmail_returnsEmptyWhenNotPresent() {
        Optional<Employee> result = employeeRepository.findByEmail("nope@corp.com");

        assertThat(result).isEmpty();
    }

    @Test
    void save_throwsWhenEmailAlreadyExists() {
        employeeRepository.saveAndFlush(new Employee("Ana", "Pop", "ana@corp.com", "Eng"));

        Employee duplicate = new Employee("Alt", "Nume", "ana@corp.com", "Sales");

        assertThatThrownBy(() -> employeeRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findAllWithSpecification_filtersByDepartmentAndLastNameContains() {
        employeeRepository.save(new Employee("Ana", "Popescu", "ana@corp.com", "Eng"));
        employeeRepository.save(new Employee("Ion", "Pop", "ion@corp.com", "Eng"));
        employeeRepository.save(new Employee("Maria", "Popescu", "maria@corp.com", "Sales"));

        Specification<Employee> spec = Specification.allOf(
                EmployeeSpecifications.inDepartment("eng"),
                EmployeeSpecifications.lastNameContains("pop")
        );
        Page<Employee> result = employeeRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(Employee::getFirstName)
                .containsExactlyInAnyOrder("Ana", "Ion");
    }

    @Test
    void findAllWithSpecification_hasEmailIsCaseInsensitiveExactMatch() {
        employeeRepository.save(new Employee("Ana", "Pop", "ana@corp.com", "Eng"));
        employeeRepository.save(new Employee("Ion", "Ionescu", "ion@corp.com", "Eng"));

        Specification<Employee> spec = EmployeeSpecifications.hasEmail("ANA@CORP.COM");
        Page<Employee> result = employeeRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(Employee::getEmail)
                .containsExactly("ana@corp.com");
    }
}
