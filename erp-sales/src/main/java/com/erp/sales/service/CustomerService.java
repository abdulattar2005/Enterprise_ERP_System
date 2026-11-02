package com.erp.sales.service;

import com.erp.core.audit.AuditService;
import com.erp.core.exception.BusinessRuleException;
import com.erp.core.exception.ResourceNotFoundException;
import com.erp.sales.domain.Customer;
import com.erp.sales.dto.CustomerRequest;
import com.erp.sales.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository repository;
    private final AuditService audit;

    public CustomerService(CustomerRepository repository, AuditService audit) {
        this.repository = repository;
        this.audit = audit;
    }

    public Customer create(CustomerRequest r) {
        if (r.email() != null && !r.email().isBlank() && repository.existsByEmailIgnoreCase(r.email())) {
            throw new BusinessRuleException("A customer with this email already exists: " + r.email());
        }
        Customer c = new Customer();
        c.setName(r.name().trim());
        c.setEmail(r.email() == null || r.email().isBlank() ? null : r.email().trim().toLowerCase());
        c.setPhone(r.phone());
        c.setCompany(r.company());
        Customer saved = repository.save(c);
        audit.log("CREATE_CUSTOMER", "Created customer " + saved.getName() + " (id=" + saved.getId() + ")");
        return saved;
    }

    @Transactional(readOnly = true)
    public Customer get(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Customer", id));
    }

    @Transactional(readOnly = true)
    public Page<Customer> list(Pageable pageable) {
        return repository.findAll(pageable);
    }
}
