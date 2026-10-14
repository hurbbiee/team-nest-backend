package com.teamnest.hrm.department;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

import com.teamnest.hrm.common.response.ApiResponse;
import com.teamnest.hrm.common.response.PageResponse;
import com.teamnest.hrm.department.dto.CreateDepartmentRequest;
import com.teamnest.hrm.department.dto.DeleteDepartmentResponse;
import com.teamnest.hrm.department.dto.DepartmentResponse;
import com.teamnest.hrm.department.dto.UpdateDepartmentRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentResponse>> create(
            @Valid @RequestBody CreateDepartmentRequest request) {
        DepartmentResponse department = departmentService.create(request);
        ApiResponse<DepartmentResponse> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Department created successfully",
                department);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> findById(@PathVariable Long id) {
        DepartmentResponse department = departmentService.findById(id);
        ApiResponse<DepartmentResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Department retrieved successfully",
                department);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDepartmentRequest request) {
        DepartmentResponse department = departmentService.update(id, request);
        ApiResponse<DepartmentResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Department updated successfully",
                department);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<DeleteDepartmentResponse>> delete(@PathVariable Long id) {
        DeleteDepartmentResponse department = departmentService.delete(id);
        ApiResponse<DeleteDepartmentResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Department deleted successfully",
                department);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DepartmentResponse>>> findAll(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String keyword) {
        PageResponse<DepartmentResponse> departments = departmentService.findAll(page, size, keyword);
        ApiResponse<PageResponse<DepartmentResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Departments retrieved successfully",
                departments);

        return ResponseEntity.ok(response);
    }
}
