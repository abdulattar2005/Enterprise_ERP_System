package com.erp.accounting.dto;

import java.math.BigDecimal;

/** One side of a journal entry, referencing an account by its code. */
public record LineSpec(String accountCode, BigDecimal debit, BigDecimal credit) {
}
