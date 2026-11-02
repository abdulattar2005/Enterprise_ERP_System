package com.erp.accounting.dto;

import com.erp.accounting.domain.JournalEntry;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record JournalEntryDto(Long id, LocalDate entryDate, String description, String reference, List<Line> lines) {

    public record Line(String accountCode, String accountName, BigDecimal debit, BigDecimal credit) {
    }

    public static JournalEntryDto from(JournalEntry e) {
        List<Line> lines = e.getLines().stream()
                .map(l -> new Line(l.getAccount().getCode(), l.getAccount().getName(), l.getDebit(), l.getCredit()))
                .toList();
        return new JournalEntryDto(e.getId(), e.getEntryDate(), e.getDescription(), e.getReference(), lines);
    }
}
