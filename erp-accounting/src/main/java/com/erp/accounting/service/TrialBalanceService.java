package com.erp.accounting.service;

import com.erp.accounting.dto.TrialBalanceReport;
import com.erp.accounting.dto.TrialBalanceRow;
import com.erp.accounting.repository.JournalLineRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrialBalanceService {

    private final JournalLineRepository lines;

    public TrialBalanceService(JournalLineRepository lines) {
        this.lines = lines;
    }

    @Transactional(readOnly = true)
    public TrialBalanceReport report() {
        List<TrialBalanceRow> rows = lines.trialBalance();
        BigDecimal debit = rows.stream().map(TrialBalanceRow::totalDebit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal credit = rows.stream().map(TrialBalanceRow::totalCredit).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new TrialBalanceReport(rows, debit, credit, debit.compareTo(credit) == 0);
    }
}
