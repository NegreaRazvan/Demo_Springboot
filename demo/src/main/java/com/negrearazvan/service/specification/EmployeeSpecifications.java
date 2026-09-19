package com.negrearazvan.service.specification;

import com.negrearazvan.model.Employee;
import org.springframework.data.jpa.domain.Specification;

public final class EmployeeSpecifications {

    private EmployeeSpecifications() {}

    public static Specification<Employee> hasEmail(String email) {
        if (email == null || email.isBlank()) return (root, query, cb) -> cb.conjunction();
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(criteriaBuilder.lower(root.get("email")), email.toLowerCase());
    }

    public static Specification<Employee> inDepartment(String department) {
        if (department == null || department.isBlank()) return (root, query, cb) -> cb.conjunction();
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(criteriaBuilder.lower(root.get("department")), department.toLowerCase());
    }

    public static Specification<Employee> lastNameContains(String lastName) {
        if (lastName == null || lastName.isBlank()) return (root, query, cb) -> cb.conjunction();
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%");
    }
}
