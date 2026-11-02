package com.erp.hr.domain;

import com.erp.core.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payroll",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "period"}))
@Getter
@Setter
public class Payroll extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    /** Pay period in yyyy-MM form. */
    @Column(nullable = false, length = 7)
    private String period;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal grossPay;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal tax;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal netPay;
}
