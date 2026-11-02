package com.erp.hr.dto;

import com.erp.hr.domain.Payroll;
import java.math.BigDecimal;

public record PayrollDto(Long id, Long employeeId, String employeeName, String period,
                         BigDecimal grossPay, BigDecimal tax, BigDecimal netPay) {

    public static PayrollDto from(Payroll p) {
        return new PayrollDto(p.getId(), p.getEmployee().getId(), p.getEmployee().getFullName(), p.getPeriod(),
                p.getGrossPay(), p.getTax(), p.getNetPay());
    }
}
