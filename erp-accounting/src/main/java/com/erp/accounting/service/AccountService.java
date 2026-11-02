package com.erp.accounting.service;

import com.erp.accounting.domain.Account;
import com.erp.accounting.dto.AccountRequest;
import com.erp.accounting.repository.AccountRepository;
import com.erp.core.audit.AuditService;
import com.erp.core.exception.BusinessRuleException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountService {

    private final AccountRepository repository;
    private final AuditService audit;

    public AccountService(AccountRepository repository, AuditService audit) {
        this.repository = repository;
        this.audit = audit;
    }

    public Account create(AccountRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new BusinessRuleException("An account with this code already exists: " + request.code());
        }
        Account account = new Account();
        account.setCode(request.code().trim());
        account.setName(request.name().trim());
        account.setType(request.type());
        Account saved = repository.save(account);
        audit.log("CREATE_ACCOUNT", "Created account " + saved.getCode() + " " + saved.getName());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Account> list() {
        return repository.findAll();
    }
}
