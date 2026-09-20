package com.negrearazvan.service;

import com.negrearazvan.model.Employee;
import com.negrearazvan.model.dto.EmployeeFilter;
import com.negrearazvan.model.dto.EmployeeRequest;
import com.negrearazvan.model.dto.EmployeeResponse;
import com.negrearazvan.model.dto.PageResponse;
import com.negrearazvan.model.exception.EmailAlreadyExistsException;
import com.negrearazvan.model.exception.ResourceNotFoundException;
import com.negrearazvan.repository.EmployeeRepository;
import com.negrearazvan.service.mapper.EmployeeMapper;
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

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void testGetAllEmployees() {
        Employee employee1 = new Employee("Ana", "Pop", "ana@corp.com", "Eng");
        Employee employee2 = new Employee("Ion", "Ionescu", "ion@corp.com", "Sales");

        when(employeeRepository.findAll()).thenReturn(List.of(employee1, employee2));

        EmployeeResponse response1 = new EmployeeResponse(1L, "Ana", "Pop", "ana@corp.com", "Eng");
        EmployeeResponse response2 = new EmployeeResponse(2L, "Ion", "Ionescu", "ion@corp.com", "Sales");
        when(employeeMapper.toResponse(employee1)).thenReturn(response1);
        when(employeeMapper.toResponse(employee2)).thenReturn(response2);

        List<EmployeeResponse> result = employeeService.getAllEmployees();

        assertThat(result).containsExactly(response1, response2);
    }

    @Test
    void search_mapsRepositoryPageToPageResponse() {
        Employee ana = new Employee("Ana", "Pop", "ana@corp.com", "Eng");
        Employee ion = new Employee("Ion", "Ionescu", "ion@corp.com", "Sales");

        Pageable pageable = PageRequest.of(0, 2, Sort.by("lastName"));
        Page<Employee> repositoryPage = new PageImpl<>(List.of(ana, ion), pageable, 5);

        when(employeeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(repositoryPage);
        when(employeeMapper.toResponse(ana)).thenReturn(new EmployeeResponse(1L, "Ana", "Pop", "ana@corp.com", "Eng"));
        when(employeeMapper.toResponse(ion)).thenReturn(new EmployeeResponse(2L, "Ion", "Ionescu", "ion@corp.com", "Sales"));

        PageResponse<EmployeeResponse> result = employeeService.search(new EmployeeFilter(null, null, null), pageable);

        assertThat(result.content())
                .extracting(EmployeeResponse::lastName)
                .containsExactly("Pop", "Ionescu");
        assertThat(result.pageNumber()).isEqualTo(0);
        assertThat(result.pageSize()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(5);
        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.last()).isFalse();
    }

    @Test
    void search_returnsEmptyPageResponseWhenNoMatches() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("lastName"));
        Page<Employee> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(employeeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        PageResponse<EmployeeResponse> result = employeeService.search(new EmployeeFilter(null, "Nope", null), pageable);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
        assertThat(result.totalPages()).isZero();
        assertThat(result.last()).isTrue();
    }

    @Test
    void search_forwardsThePageableItReceivedUnchanged() {
        Pageable pageable = PageRequest.of(2, 5, Sort.by(Sort.Direction.DESC, "lastName"));
        when(employeeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

        employeeService.search(new EmployeeFilter(null, null, null), pageable);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(employeeRepository).findAll(any(Specification.class), pageableCaptor.capture());

        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(5);
        assertThat(pageableCaptor.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "lastName"));
    }

    @Test
    void getEmployeeById_returnsMappedResponseWhenFound() {
        Employee employee = new Employee("Ana", "Pop", "ana@corp.com", "Eng");
        EmployeeResponse response = new EmployeeResponse(1L, "Ana", "Pop", "ana@corp.com", "Eng");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(response);

        EmployeeResponse result = employeeService.getEmployeeById(1L);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void getEmployeeById_throwsWhenNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("employee");

        verifyNoInteractions(employeeMapper);
    }

    @Test
    void testCreateEmployee() {
        EmployeeRequest request = new EmployeeRequest("Ana", "Pop", "ana@corp.com", "Eng");
        Employee entity = new Employee("Ana", "Pop", "ana@corp.com", "Eng");
        Employee savedEntity = new Employee("Ana", "Pop", "ana@corp.com", "Eng");
        EmployeeResponse response = new EmployeeResponse(1L, "Ana", "Pop", "ana@corp.com", "Eng");

        when(employeeRepository.findByEmail("ana@corp.com")).thenReturn(Optional.empty());
        when(employeeMapper.toEntity(request)).thenReturn(entity);
        when(employeeRepository.save(entity)).thenReturn(savedEntity);
        when(employeeMapper.toResponse(savedEntity)).thenReturn(response);

        EmployeeResponse result = employeeService.createEmployee(request);

        assertThat(result).isEqualTo(response);
        verify(employeeRepository).save(entity);
    }

    @Test
    void createEmployee_throwsWhenEmailAlreadyExists() {
        EmployeeRequest request = new EmployeeRequest("Ana", "Pop", "ana@corp.com", "Eng");
        Employee existing = new Employee("Ana", "Popescu", "ana@corp.com", "Sales");

        when(employeeRepository.findByEmail("ana@corp.com")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("ana@corp.com");

        verify(employeeRepository, never()).save(any());
        verifyNoInteractions(employeeMapper);
    }

    @Test
    void getEmployeeByEmail_returnsSingletonListWhenFound() {
        Employee employee = new Employee("Ana", "Pop", "ana@corp.com", "Eng");
        EmployeeResponse response = new EmployeeResponse(1L, "Ana", "Pop", "ana@corp.com", "Eng");

        when(employeeRepository.findByEmail("ana@corp.com")).thenReturn(Optional.of(employee));
        when(employeeMapper.toResponse(employee)).thenReturn(response);

        List<EmployeeResponse> result = employeeService.getEmployeeByEmail("ANA@corp.com");

        assertThat(result).containsExactly(response);
    }

    @Test
    void getEmployeeByEmail_returnsEmptyListWhenNotFound() {
        when(employeeRepository.findByEmail("nope@corp.com")).thenReturn(Optional.empty());

        List<EmployeeResponse> result = employeeService.getEmployeeByEmail("nope@corp.com");

        assertThat(result).isEmpty();
        verifyNoInteractions(employeeMapper);
    }
}
