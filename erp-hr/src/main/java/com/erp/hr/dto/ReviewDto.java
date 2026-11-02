package com.erp.hr.dto;

import com.erp.hr.domain.PerformanceReview;

public record ReviewDto(Long id, Long employeeId, String period, int rating, String comments, String reviewer) {

    public static ReviewDto from(PerformanceReview r) {
        return new ReviewDto(r.getId(), r.getEmployee().getId(), r.getPeriod(), r.getRating(), r.getComments(),
                r.getReviewer());
    }
}
