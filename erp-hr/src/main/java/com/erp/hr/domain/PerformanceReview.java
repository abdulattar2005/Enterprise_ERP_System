package com.erp.hr.domain;

import com.erp.core.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "performance_reviews")
@Getter
@Setter
public class PerformanceReview extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(nullable = false, length = 32)
    private String period;

    /** 1 (poor) to 5 (outstanding). */
    @Column(nullable = false)
    private int rating;

    @Column(length = 2000)
    private String comments;

    @Column(nullable = false)
    private String reviewer;
}
