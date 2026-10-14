package com.teamnest.hrm.department.dto;

import java.time.Instant;

public record DepartmentResponse(
        Long id,
        String code,
        String name,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
}
