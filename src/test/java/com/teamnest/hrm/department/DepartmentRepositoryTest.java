package com.teamnest.hrm.department;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class DepartmentRepositoryTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    void shouldSaveAndFindNonDeletedDepartment() {
        String code = "TEST_" + System.nanoTime();
        Department department = new Department(code, "Repository Test Department");

        Department savedDepartment = departmentRepository.saveAndFlush(department);
        Optional<Department> foundDepartment = departmentRepository
                .findByIdAndDeletedFalse(savedDepartment.getId());

        assertNotNull(savedDepartment.getId());
        assertNotNull(savedDepartment.getCreatedAt());
        assertNotNull(savedDepartment.getUpdatedAt());
        assertTrue(savedDepartment.isActive());
        assertFalse(savedDepartment.isDeleted());
        assertTrue(foundDepartment.isPresent());
        assertEquals(code, foundDepartment.orElseThrow().getCode());
    }

    @Test
    void shouldSearchNonDeletedDepartmentsByKeyword() {
        String uniqueKeyword = "SEARCH" + System.nanoTime();
        Department department = new Department(uniqueKeyword, uniqueKeyword + " Department");
        departmentRepository.saveAndFlush(department);

        Page<Department> result = departmentRepository.searchNonDeleted(
                uniqueKeyword.toLowerCase(),
                PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(uniqueKeyword, result.getContent().getFirst().getCode());
    }

    @Test
    void shouldSoftDeleteDepartmentWithoutRemovingDatabaseRow() {
        String code = "DELETE_" + System.nanoTime();
        Department department = departmentRepository.saveAndFlush(
                new Department(code, "Soft Delete Test Department"));
        Long id = department.getId();

        department.softDelete();
        departmentRepository.flush();

        assertTrue(departmentRepository.findByIdAndDeletedFalse(id).isEmpty());

        Department deletedDepartment = departmentRepository.findById(id).orElseThrow();
        assertTrue(deletedDepartment.isDeleted());
        assertFalse(deletedDepartment.isActive());
        assertNotNull(deletedDepartment.getDeletedAt());
        assertTrue(!deletedDepartment.getUpdatedAt().isBefore(deletedDepartment.getDeletedAt()));
    }
}
