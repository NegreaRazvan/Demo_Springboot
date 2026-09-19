package com.negrearazvan.model;

import com.negrearazvan.model.enumeration.EnrollmentStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "enrollments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "course_id"}))
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    private Instant enrolledAt;

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;

    protected Enrollment() {}

    public Enrollment(Employee employee, Course course) {
        this.employee = employee;
        this.course = course;
        this.enrolledAt = Instant.now();
        this.status = EnrollmentStatus.ENROLLED;
    }

    public Long getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Course getCourse() {
        return course;
    }

    public Instant getEnrolledAt() {
        return enrolledAt;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }


}
