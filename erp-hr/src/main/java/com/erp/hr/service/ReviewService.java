package com.erp.hr.service;

import com.erp.core.audit.AuditService;
import com.erp.core.exception.ResourceNotFoundException;
import com.erp.hr.domain.Employee;
import com.erp.hr.domain.PerformanceReview;
import com.erp.hr.dto.ReviewDto;
import com.erp.hr.dto.ReviewRequest;
import com.erp.hr.repository.EmployeeRepository;
import com.erp.hr.repository.PerformanceReviewRepository;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewService {

    private final PerformanceReviewRepository reviewRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditService audit;

    public ReviewService(PerformanceReviewRepository reviewRepository, EmployeeRepository employeeRepository,
                         AuditService audit) {
        this.reviewRepository = reviewRepository;
        this.employeeRepository = employeeRepository;
        this.audit = audit;
    }

    public ReviewDto create(ReviewRequest request) {
        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> ResourceNotFoundException.of("Employee", request.employeeId()));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        PerformanceReview review = new PerformanceReview();
        review.setEmployee(employee);
        review.setPeriod(request.period());
        review.setRating(request.rating());
        review.setComments(request.comments());
        review.setReviewer(auth != null ? auth.getName() : "system");
        PerformanceReview saved = reviewRepository.save(review);
        audit.log("CREATE_REVIEW", "Review for employee id=" + employee.getId() + ", rating=" + request.rating());
        return ReviewDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> forEmployee(Long employeeId) {
        return reviewRepository.findByEmployeeIdOrderByIdDesc(employeeId).stream().map(ReviewDto::from).toList();
    }
}
