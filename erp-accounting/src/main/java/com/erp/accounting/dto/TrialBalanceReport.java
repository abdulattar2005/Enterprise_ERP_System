package com.erp.accounting.dto;

import java.math.BigDecimal;
import java.util.List;

/** The ledger is healthy when total debits equal total credits. */
public record TrialBalanceReport(List<TrialBalanceRow> rows, BigDecimal totalDebit, BigDecimal totalCredit,
                                 boolean balanced) {
}
