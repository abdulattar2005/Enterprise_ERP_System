package com.erp.inventory.web;

import com.erp.inventory.domain.Warehouse;
import com.erp.inventory.dto.WarehouseRequest;
import com.erp.inventory.service.WarehouseService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory/warehouses")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class WarehouseController {

    private final WarehouseService service;

    public WarehouseController(WarehouseService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Warehouse create(@Valid @RequestBody WarehouseRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<Warehouse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public Warehouse get(@PathVariable Long id) {
        return service.get(id);
    }
}
