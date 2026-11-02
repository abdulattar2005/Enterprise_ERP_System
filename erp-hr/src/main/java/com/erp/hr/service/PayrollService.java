package com.erp.hr.service;

import com.erp.core.audit.AuditService;
import com.erp.core.event.PayrollProcessedEvent;
import com.erp.core.exception.BusinessRuleException;
import com.erp.hr.domain.Employee;
import com.erp.hr.domain.EmployeeStatus;
import com.erp.hr.domain.Payroll;
import com.erp.hr.dto.PayrollDto;
import com.erp.hr.repository.EmployeeRepository;
import com.erp.hr.repository.PayrollRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PayrollService {

    /** One marginal tax band. {@code upTo == null} means "no upper limit". */
    record Slab(BigDecimal upTo, BigDecimal rate) {
    }

    /**
     * Illustrative progressive monthly tax bands (marginal rates). These are demo values, not a statement
     * of any country's tax law; a real deployment would load them from configuration.
     */
    private static final List<Slab> SLABS = List.of(
            new Slab(new BigDecimal("25000"), new BigDecimal("0.05")),
            new Slab(new BigDecimal("75000"), new BigDecimal("0.10")),
            new Slab(null, new BigDecimal("0.20")));

    private final EmployeeRepository employeeRepository;
    private final PayrollRepository payrollRepository;
    private final AuditService audit;
    private final ApplicationEventPublisher events;

    public PayrollService(EmployeeRepository employeeRepository, PayrollRepository payrollRepository,
                          AuditService audit, ApplicationEventPublisher events) {
        this.employeeRepository = employeeRepository;
        this.payrollRepository = payrollRepository;
        this.audit = audit;
        this.events = events;
    }

    /**
     * Marginal-slab tax: each band taxes only the part of the salary that falls inside it.
     * Example: gross 100000 -> 25000*5% + 50000*10% + 25000*20% = 11250.00
     */
    public static BigDecimal calculateTax(BigDecimal gross) {
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal lower = BigDecimal.ZERO;
        for (Slab slab : SLABS) {
            if (gross.compareTo(lower) <= 0) {
                break;
            }
            BigDecimal upper = slab.upTo() == null ? gross : gross.min(slab.upTo());
            tax = tax.add(upper.subtract(lower).multiply(slab.rate()));
            lower = slab.upTo() == null ? gross : slab.upTo();
        }
        return tax.setScale(2, RoundingMode.HALF_UP);
    }

    /** Idempotent: employees that already have a payroll row for the period are skipped. */
    public List<PayrollDto> run(String period) {
        try {
            YearMonth.parse(period);
        } catch (DateTimeParseException e) {
            throw new BusinessRuleException("Period must be in yyyy-MM format, e.g. 2026-10");
        }
        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;
        int created = 0;
        for (Employee employee : employeeRepository.findByStatus(EmployeeStatus.ACTIVE)) {
            if (payrollRepository.existsByEmployeeIdAndPeriod(employee.getId(), period)) {
                continue;
            }
            BigDecimal gross = employee.getMonthlySalary().setScale(2, RoundingMode.HALF_UP);
            BigDecimal tax = calculateTax(gross);
            Payroll payroll = new Payroll();
            payroll.setEmployee(employee);
            payroll.setPeriod(period);
            payroll.setGrossPay(gross);
            payroll.setTax(tax);
            payroll.setNetPay(gross.subtract(tax));
            payrollRepository.save(payroll);
            totalGross = totalGross.add(gross);
            totalTax = totalTax.add(tax);
            totalNet = totalNet.add(payroll.getNetPay());
            created++;
        }
        if (created > 0) {
            events.publishEvent(new PayrollProcessedEvent(period, totalGross, totalTax, totalNet));
            audit.log("RUN_PAYROLL", "Payroll " + period + ": " + created + " employees, gross=" + totalGross);
        }
        return forPeriod(period);
    }

    @Transactional(readOnly = true)
    public List<PayrollDto> forPeriod(String period) {
        return new ArrayList<>(payrollRepository.findByPeriodOrderByEmployeeId(period).stream()
                .map(PayrollDto::from).toList());
    }
}
