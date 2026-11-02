package com.erp.accounting.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record JournalEntryRequest(LocalDate entryDate, @NotBlank String description, String reference,
                                  @NotEmpty @Valid List<Line> lines) {

    public record Line(@NotBlank String accountCode, BigDecimal debit, BigDecimal credit) {
    }
}
