package com.teamnest.hrm.department.dto;

import java.time.Instant;

public record DeleteDepartmentResponse(
        Long id,
        boolean deleted,
        Instant deletedAt) {
}
