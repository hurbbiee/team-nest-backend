package com.teamnest.hrm.department;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByCode(String code);

    Optional<Department> findByIdAndDeletedFalse(Long id);

    @Query("""
            SELECT department
            FROM Department department
            WHERE department.deleted = false
              AND (
                  :keyword = ''
                  OR LOWER(department.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(department.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    Page<Department> searchNonDeleted(@Param("keyword") String keyword, Pageable pageable);
}
