package com.teamnest.hrm.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateDepartmentRequest(
        @NotBlank(message = "Department name is required")
        @Size(max = 150, message = "Department name must not exceed 150 characters")
        @Pattern(
                regexp = "^[^\\s](?:.*[^\\s])?$",
                message = "Department name must not have leading or trailing whitespace")
        String name,

        @NotNull(message = "Department active status is required")
        Boolean active) {
}
