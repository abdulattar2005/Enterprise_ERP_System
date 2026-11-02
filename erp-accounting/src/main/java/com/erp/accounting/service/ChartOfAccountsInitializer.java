package com.erp.accounting.service;

import com.erp.accounting.domain.Account;
import com.erp.accounting.domain.AccountType;
import com.erp.accounting.repository.AccountRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Creates the accounts the automatic postings need. Idempotent: existing codes are left alone. */
@Component
public class ChartOfAccountsInitializer implements ApplicationRunner {

    private final AccountRepository repository;

    public ChartOfAccountsInitializer(AccountRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensure("1000", "Cash", AccountType.ASSET);
        ensure("1100", "Accounts Receivable", AccountType.ASSET);
        ensure("1200", "Inventory", AccountType.ASSET);
        ensure("2000", "Accounts Payable", AccountType.LIABILITY);
        ensure("2100", "Taxes Payable", AccountType.LIABILITY);
        ensure("3000", "Owner's Equity", AccountType.EQUITY);
        ensure("4000", "Sales Revenue", AccountType.REVENUE);
        ensure("5000", "Cost of Goods Sold", AccountType.EXPENSE);
        ensure("5100", "Salaries Expense", AccountType.EXPENSE);
    }

    private void ensure(String code, String name, AccountType type) {
        if (!repository.existsByCode(code)) {
            Account a = new Account();
            a.setCode(code);
            a.setName(name);
            a.setType(type);
            repository.save(a);
        }
    }
}
