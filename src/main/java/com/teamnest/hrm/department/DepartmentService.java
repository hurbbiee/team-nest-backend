package com.teamnest.hrm.department;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teamnest.hrm.common.response.PageResponse;
import com.teamnest.hrm.department.dto.CreateDepartmentRequest;
import com.teamnest.hrm.department.dto.DeleteDepartmentResponse;
import com.teamnest.hrm.department.dto.DepartmentResponse;
import com.teamnest.hrm.department.dto.UpdateDepartmentRequest;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public DepartmentResponse create(CreateDepartmentRequest request) {
        if (departmentRepository.existsByCode(request.code())) {
            throw new DepartmentCodeAlreadyExistsException(request.code());
        }

        Department department = new Department(request.code(), request.name());
        Department savedDepartment = departmentRepository.save(department);

        return toResponse(savedDepartment);
    }

    @Transactional(readOnly = true)
    public DepartmentResponse findById(Long id) {
        Department department = departmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new DepartmentNotFoundException(id));

        return toResponse(department);
    }

    @Transactional
    public DepartmentResponse update(Long id, UpdateDepartmentRequest request) {
        Department department = departmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new DepartmentNotFoundException(id));

        department.update(request.name(), request.active());
        departmentRepository.flush();

        return toResponse(department);
    }

    @Transactional
    public DeleteDepartmentResponse delete(Long id) {
        Department department = departmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new DepartmentNotFoundException(id));

        department.softDelete();
        departmentRepository.flush();

        return new DeleteDepartmentResponse(
                department.getId(),
                department.isDeleted(),
                department.getDeletedAt());
    }

    @Transactional(readOnly = true)
    public PageResponse<DepartmentResponse> findAll(int page, int size, String keyword) {
        Sort sort = Sort.by("name").ascending()
                .and(Sort.by("id").ascending());
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        String normalizedKeyword = keyword == null ? "" : keyword.trim();

        Page<Department> departmentPage = departmentRepository
                .searchNonDeleted(normalizedKeyword, pageable);
        List<DepartmentResponse> content = departmentPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                departmentPage.getNumber() + 1,
                departmentPage.getSize(),
                departmentPage.getTotalElements(),
                departmentPage.getTotalPages());
    }

    private DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getCode(),
                department.getName(),
                department.isActive(),
                department.getCreatedAt(),
                department.getUpdatedAt());
    }
}
