package com.teamnest.hrm.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateDepartmentRequest(
        @NotBlank(message = "Department code is required")
        @Size(max = 50, message = "Department code must not exceed 50 characters")
        @Pattern(
                regexp = "^[A-Z][A-Z0-9_-]{0,49}$",
                message = "Department code must start with an uppercase letter and contain only uppercase letters, numbers, _ or -")
        String code,

        @NotBlank(message = "Department name is required")
        @Size(max = 150, message = "Department name must not exceed 150 characters")
        @Pattern(
                regexp = "^[^\\s](?:.*[^\\s])?$",
                message = "Department name must not have leading or trailing whitespace")
        String name) {
}
