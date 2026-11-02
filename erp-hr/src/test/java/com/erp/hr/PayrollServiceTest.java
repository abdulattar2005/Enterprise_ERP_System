package com.erp.hr;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.erp.hr.service.PayrollService;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PayrollServiceTest {

    @Test
    @DisplayName("Salary inside the first band is taxed at the first rate only")
    void taxInsideFirstBand() {
        assertEquals(new BigDecimal("1000.00"), PayrollService.calculateTax(new BigDecimal("20000")));
    }

    @Test
    @DisplayName("Salary spanning two bands is taxed marginally")
    void taxAcrossTwoBands() {
        // 25000 * 5% + 25000 * 10% = 1250 + 2500
        assertEquals(new BigDecimal("3750.00"), PayrollService.calculateTax(new BigDecimal("50000")));
    }

    @Test
    @DisplayName("Salary spanning all bands is taxed marginally")
    void taxAcrossAllBands() {
        // 25000*5% + 50000*10% + 25000*20% = 1250 + 5000 + 5000
        assertEquals(new BigDecimal("11250.00"), PayrollService.calculateTax(new BigDecimal("100000")));
    }

    @Test
    @DisplayName("Zero salary has zero tax")
    void zeroSalary() {
        assertEquals(new BigDecimal("0.00"), PayrollService.calculateTax(BigDecimal.ZERO));
    }
}
