package com.teamnest.hrm.common.error;

public record ApiErrorResponse(
        int status,
        String message) {
}
