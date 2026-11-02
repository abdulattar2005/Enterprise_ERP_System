package com.erp.accounting;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.erp.accounting.dto.LineSpec;
import com.erp.accounting.service.JournalService;
import com.erp.core.exception.BusinessRuleException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JournalServiceTest {

    private static LineSpec debit(String code, String amount) {
        return new LineSpec(code, new BigDecimal(amount), BigDecimal.ZERO);
    }

    private static LineSpec credit(String code, String amount) {
        return new LineSpec(code, BigDecimal.ZERO, new BigDecimal(amount));
    }

    @Test
    @DisplayName("A balanced two-line entry is accepted")
    void balanced() {
        assertDoesNotThrow(() -> JournalService.validateBalanced(
                List.of(debit("1100", "100.00"), credit("4000", "100.00"))));
    }

    @Test
    @DisplayName("Equal totals across several lines are accepted (100.0 equals 100.00)")
    void balancedSplit() {
        assertDoesNotThrow(() -> JournalService.validateBalanced(
                List.of(debit("5100", "100.0"), credit("1000", "90.00"), credit("2100", "10.00"))));
    }

    @Test
    @DisplayName("An unbalanced entry is rejected")
    void unbalanced() {
        assertThrows(BusinessRuleException.class, () -> JournalService.validateBalanced(
                List.of(debit("1100", "100.00"), credit("4000", "99.99"))));
    }

    @Test
    @DisplayName("A single-line entry is rejected")
    void singleLine() {
        assertThrows(BusinessRuleException.class,
                () -> JournalService.validateBalanced(List.of(debit("1100", "100.00"))));
    }

    @Test
    @DisplayName("A line with both debit and credit is rejected")
    void bothSides() {
        LineSpec both = new LineSpec("1100", new BigDecimal("5"), new BigDecimal("5"));
        assertThrows(BusinessRuleException.class,
                () -> JournalService.validateBalanced(List.of(both, credit("4000", "0.01"))));
    }

    @Test
    @DisplayName("Negative amounts are rejected")
    void negative() {
        LineSpec negative = new LineSpec("1100", new BigDecimal("-5"), BigDecimal.ZERO);
        assertThrows(BusinessRuleException.class,
                () -> JournalService.validateBalanced(List.of(negative, credit("4000", "5"))));
    }
}
