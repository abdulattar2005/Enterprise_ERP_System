package com.erp.accounting.service;

import com.erp.accounting.domain.Account;
import com.erp.accounting.domain.JournalEntry;
import com.erp.accounting.domain.JournalLine;
import com.erp.accounting.dto.JournalEntryDto;
import com.erp.accounting.dto.JournalEntryRequest;
import com.erp.accounting.dto.LineSpec;
import com.erp.accounting.repository.AccountRepository;
import com.erp.accounting.repository.JournalEntryRepository;
import com.erp.core.audit.AuditService;
import com.erp.core.exception.BusinessRuleException;
import com.erp.core.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JournalService {

    private final AccountRepository accounts;
    private final JournalEntryRepository entries;
    private final AuditService audit;
    private final Clock clock;

    public JournalService(AccountRepository accounts, JournalEntryRepository entries, AuditService audit,
                          Clock clock) {
        this.accounts = accounts;
        this.entries = entries;
        this.audit = audit;
        this.clock = clock;
    }

    /** Manual entry posted through the API. */
    public JournalEntryDto post(JournalEntryRequest request) {
        List<LineSpec> specs = request.lines().stream()
                .map(l -> new LineSpec(l.accountCode(), l.debit(), l.credit())).toList();
        LocalDate date = request.entryDate() != null ? request.entryDate() : LocalDate.now(clock);
        return JournalEntryDto.from(save(date, request.description(), request.reference(), specs));
    }

    /** Entry created by another module's event (sales, purchases, payroll). */
    public JournalEntry postSystemEntry(String description, String reference, List<LineSpec> specs) {
        return save(LocalDate.now(clock), description, reference, specs);
    }

    private JournalEntry save(LocalDate date, String description, String reference, List<LineSpec> specs) {
        validateBalanced(specs);
        JournalEntry entry = new JournalEntry();
        entry.setEntryDate(date);
        entry.setDescription(description);
        entry.setReference(reference);
        for (LineSpec spec : specs) {
            Account account = accounts.findByCode(spec.accountCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Account not found with code " + spec.accountCode()));
            if (!account.isActive()) {
                throw new BusinessRuleException("Account " + account.getCode() + " is inactive");
            }
            JournalLine line = new JournalLine();
            line.setAccount(account);
            line.setDebit(nz(spec.debit()));
            line.setCredit(nz(spec.credit()));
            entry.addLine(line);
        }
        JournalEntry saved = entries.save(entry);
        audit.log("POST_JOURNAL", "Journal entry id=" + saved.getId() + " ref=" + reference);
        return saved;
    }

    /**
     * Double-entry rule: at least two lines, each line is exactly one of debit or credit (never negative),
     * and total debits equal total credits.
     */
    public static void validateBalanced(List<LineSpec> lines) {
        if (lines == null || lines.size() < 2) {
            throw new BusinessRuleException("A journal entry needs at least two lines");
        }
        BigDecimal debits = BigDecimal.ZERO;
        BigDecimal credits = BigDecimal.ZERO;
        for (LineSpec line : lines) {
            BigDecimal d = nz(line.debit());
            BigDecimal c = nz(line.credit());
            if (d.signum() < 0 || c.signum() < 0) {
                throw new BusinessRuleException("Debit and credit amounts must not be negative");
            }
            if ((d.signum() > 0) == (c.signum() > 0)) {
                throw new BusinessRuleException("Each line must have exactly one of debit or credit");
            }
            debits = debits.add(d);
            credits = credits.add(c);
        }
        if (debits.compareTo(credits) != 0) {
            throw new BusinessRuleException("Entry is not balanced: debits=" + debits + ", credits=" + credits);
        }
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    @Transactional(readOnly = true)
    public Page<JournalEntryDto> list(Pageable pageable) {
        return entries.findAll(pageable).map(JournalEntryDto::from);
    }
}
