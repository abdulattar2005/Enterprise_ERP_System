package com.erp.accounting.dto;

import com.erp.accounting.domain.AccountType;
import java.math.BigDecimal;

public record TrialBalanceRow(String code, String name, AccountType type, BigDecimal totalDebit,
                              BigDecimal totalCredit) {
}
