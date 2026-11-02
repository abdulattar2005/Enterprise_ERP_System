package com.erp.inventory.service;

import com.erp.core.audit.AuditService;
import com.erp.core.exception.BusinessRuleException;
import com.erp.core.exception.ResourceNotFoundException;
import com.erp.inventory.domain.Warehouse;
import com.erp.inventory.dto.WarehouseRequest;
import com.erp.inventory.repository.WarehouseRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WarehouseService {

    private final WarehouseRepository repository;
    private final AuditService audit;

    public WarehouseService(WarehouseRepository repository, AuditService audit) {
        this.repository = repository;
        this.audit = audit;
    }

    public Warehouse create(WarehouseRequest r) {
        if (repository.existsByCodeIgnoreCase(r.code())) {
            throw new BusinessRuleException("A warehouse with this code already exists: " + r.code());
        }
        Warehouse w = new Warehouse();
        w.setCode(r.code().trim().toUpperCase());
        w.setName(r.name().trim());
        w.setLocation(r.location());
        Warehouse saved = repository.save(w);
        audit.log("CREATE_WAREHOUSE", "Created warehouse " + saved.getCode());
        return saved;
    }

    @Transactional(readOnly = true)
    public Warehouse get(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Warehouse", id));
    }

    @Transactional(readOnly = true)
    public List<Warehouse> list() {
        return repository.findAll();
    }
}
