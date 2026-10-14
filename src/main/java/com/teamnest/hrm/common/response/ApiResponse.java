package com.teamnest.hrm.common.response;

public record ApiResponse<T>(
        int status,
        String message,
        T data) {
}
