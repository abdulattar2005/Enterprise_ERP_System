package com.erp.hr.repository;

import com.erp.hr.domain.Payroll;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    boolean existsByEmployeeIdAndPeriod(Long employeeId, String period);

    List<Payroll> findByPeriodOrderByEmployeeId(String period);
}
