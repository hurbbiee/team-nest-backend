package com.teamnest.hrm.department;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.teamnest.hrm.common.response.PageResponse;
import com.teamnest.hrm.department.dto.CreateDepartmentRequest;
import com.teamnest.hrm.department.dto.DeleteDepartmentResponse;
import com.teamnest.hrm.department.dto.DepartmentResponse;
import com.teamnest.hrm.department.dto.UpdateDepartmentRequest;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    private DepartmentService departmentService;

    @BeforeEach
    void setUp() {
        departmentService = new DepartmentService(departmentRepository);
    }

    @Test
    void shouldCreateDepartmentWhenCodeDoesNotExist() {
        CreateDepartmentRequest request = new CreateDepartmentRequest("ENGINEERING", "Engineering");
        when(departmentRepository.existsByCode("ENGINEERING")).thenReturn(false);
        when(departmentRepository.save(any(Department.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DepartmentResponse response = departmentService.create(request);

        assertEquals("ENGINEERING", response.code());
        assertEquals("Engineering", response.name());
        assertEquals(true, response.active());
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void shouldRejectDuplicateDepartmentCode() {
        CreateDepartmentRequest request = new CreateDepartmentRequest("ENGINEERING", "Engineering");
        when(departmentRepository.existsByCode("ENGINEERING")).thenReturn(true);

        DepartmentCodeAlreadyExistsException exception = assertThrows(
                DepartmentCodeAlreadyExistsException.class,
                () -> departmentService.create(request));

        assertEquals("Department code already exists: ENGINEERING", exception.getMessage());
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void shouldFindNonDeletedDepartmentById() {
        Department department = new Department("ENGINEERING", "Engineering");
        when(departmentRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(department));

        DepartmentResponse response = departmentService.findById(1L);

        assertEquals("ENGINEERING", response.code());
        assertEquals("Engineering", response.name());
        assertEquals(true, response.active());
        verify(departmentRepository).findByIdAndDeletedFalse(1L);
    }

    @Test
    void shouldThrowWhenDepartmentDoesNotExist() {
        when(departmentRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        DepartmentNotFoundException exception = assertThrows(
                DepartmentNotFoundException.class,
                () -> departmentService.findById(999L));

        assertEquals("Department not found with id: 999", exception.getMessage());
    }

    @Test
    void shouldFindDepartmentsWithPaginationAndKeyword() {
        Department department = new Department("ENGINEERING", "Engineering");
        when(departmentRepository.searchNonDeleted(eq("engineer"), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    Pageable pageable = invocation.getArgument(1);
                    return new PageImpl<>(List.of(department), pageable, 1);
                });

        PageResponse<DepartmentResponse> response = departmentService.findAll(1, 10, " engineer ");

        assertEquals(1, response.content().size());
        assertEquals("ENGINEERING", response.content().getFirst().code());
        assertEquals(1, response.page());
        assertEquals(10, response.size());
        assertEquals(1, response.totalElements());
        assertEquals(1, response.totalPages());
    }

    @Test
    void shouldUpdateDepartmentNameAndActiveStatus() {
        Department department = new Department("ENGINEERING", "Engineering");
        UpdateDepartmentRequest request = new UpdateDepartmentRequest("Software Engineering", false);
        when(departmentRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(department));

        DepartmentResponse response = departmentService.update(1L, request);

        assertEquals("ENGINEERING", response.code());
        assertEquals("Software Engineering", response.name());
        assertEquals(false, response.active());
        verify(departmentRepository).flush();
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void shouldThrowWhenUpdatingDepartmentThatDoesNotExist() {
        UpdateDepartmentRequest request = new UpdateDepartmentRequest("Unknown", true);
        when(departmentRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        DepartmentNotFoundException exception = assertThrows(
                DepartmentNotFoundException.class,
                () -> departmentService.update(999L, request));

        assertEquals("Department not found with id: 999", exception.getMessage());
    }

    @Test
    void shouldSoftDeleteDepartment() {
        Department department = new Department("ENGINEERING", "Engineering");
        when(departmentRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(department));

        DeleteDepartmentResponse response = departmentService.delete(1L);

        assertEquals(true, response.deleted());
        assertEquals(false, department.isActive());
        assertEquals(true, department.isDeleted());
        verify(departmentRepository).flush();
        verify(departmentRepository, never()).delete(any(Department.class));
    }

    @Test
    void shouldThrowWhenDeletingDepartmentThatDoesNotExist() {
        when(departmentRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        DepartmentNotFoundException exception = assertThrows(
                DepartmentNotFoundException.class,
                () -> departmentService.delete(999L));

        assertEquals("Department not found with id: 999", exception.getMessage());
        verify(departmentRepository, never()).delete(any(Department.class));
    }
}
