package com.erp.hr.web;

import com.erp.hr.dto.PayrollDto;
import com.erp.hr.service.PayrollService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/payroll")
@PreAuthorize("hasRole('ADMIN')")
public class PayrollController {

    private final PayrollService service;

    public PayrollController(PayrollService service) {
        this.service = service;
    }

    /** Runs payroll for a period such as 2026-10. Safe to call twice. */
    @PostMapping("/{period}/run")
    public List<PayrollDto> run(@PathVariable String period) {
        return service.run(period);
    }

    @GetMapping("/{period}")
    public List<PayrollDto> get(@PathVariable String period) {
        return service.forPeriod(period);
    }
}
