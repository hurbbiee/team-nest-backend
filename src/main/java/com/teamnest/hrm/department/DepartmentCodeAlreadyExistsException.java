package com.teamnest.hrm.department;

public class DepartmentCodeAlreadyExistsException extends RuntimeException {

    public DepartmentCodeAlreadyExistsException(String code) {
        super("Department code already exists: " + code);
    }
}
