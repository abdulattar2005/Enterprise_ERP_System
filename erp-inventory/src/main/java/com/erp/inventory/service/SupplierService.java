package com.erp.inventory.service;

import com.erp.core.audit.AuditService;
import com.erp.core.exception.BusinessRuleException;
import com.erp.core.exception.ResourceNotFoundException;
import com.erp.inventory.domain.Supplier;
import com.erp.inventory.dto.SupplierRequest;
import com.erp.inventory.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SupplierService {

    private final SupplierRepository repository;
    private final AuditService audit;

    public SupplierService(SupplierRepository repository, AuditService audit) {
        this.repository = repository;
        this.audit = audit;
    }

    public Supplier create(SupplierRequest r) {
        if (repository.existsByNameIgnoreCase(r.name())) {
            throw new BusinessRuleException("A supplier with this name already exists: " + r.name());
        }
        Supplier s = new Supplier();
        s.setName(r.name().trim());
        s.setEmail(r.email());
        s.setPhone(r.phone());
        s.setAddress(r.address());
        Supplier saved = repository.save(s);
        audit.log("CREATE_SUPPLIER", "Created supplier " + saved.getName());
        return saved;
    }

    @Transactional(readOnly = true)
    public Supplier get(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Supplier", id));
    }

    @Transactional(readOnly = true)
    public Page<Supplier> list(Pageable pageable) {
        return repository.findAll(pageable);
    }
}
