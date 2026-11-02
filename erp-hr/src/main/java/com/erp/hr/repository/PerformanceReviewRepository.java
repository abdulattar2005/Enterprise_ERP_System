package com.erp.hr.repository;

import com.erp.hr.domain.PerformanceReview;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {

    List<PerformanceReview> findByEmployeeIdOrderByIdDesc(Long employeeId);
}
