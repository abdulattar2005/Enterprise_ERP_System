package com.erp.accounting.web;

import com.erp.accounting.domain.Account;
import com.erp.accounting.dto.AccountRequest;
import com.erp.accounting.dto.JournalEntryDto;
import com.erp.accounting.dto.JournalEntryRequest;
import com.erp.accounting.dto.TrialBalanceReport;
import com.erp.accounting.service.AccountService;
import com.erp.accounting.service.JournalService;
import com.erp.accounting.service.TrialBalanceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounting")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class AccountingController {

    private final AccountService accountService;
    private final JournalService journalService;
    private final TrialBalanceService trialBalanceService;

    public AccountingController(AccountService accountService, JournalService journalService,
                                TrialBalanceService trialBalanceService) {
        this.accountService = accountService;
        this.journalService = journalService;
        this.trialBalanceService = trialBalanceService;
    }

    @GetMapping("/accounts")
    public List<Account> accounts() {
        return accountService.list();
    }

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Account createAccount(@Valid @RequestBody AccountRequest request) {
        return accountService.create(request);
    }

    @PostMapping("/journal-entries")
    @ResponseStatus(HttpStatus.CREATED)
    public JournalEntryDto post(@Valid @RequestBody JournalEntryRequest request) {
        return journalService.post(request);
    }

    @GetMapping("/journal-entries")
    public Page<JournalEntryDto> entries(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return journalService.list(pageable);
    }

    @GetMapping("/trial-balance")
    public TrialBalanceReport trialBalance() {
        return trialBalanceService.report();
    }
}
