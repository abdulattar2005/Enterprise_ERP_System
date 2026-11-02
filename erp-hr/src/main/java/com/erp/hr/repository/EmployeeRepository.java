package com.erp.hr.repository;

import com.erp.hr.domain.Employee;
import com.erp.hr.domain.EmployeeStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Page<Employee> findByDepartmentIgnoreCase(String department, Pageable pageable);

    List<Employee> findByStatus(EmployeeStatus status);

    long countByStatus(EmployeeStatus status);
}
