package com.erp.inventory.web;

import com.erp.inventory.dto.PurchaseOrderDto;
import com.erp.inventory.dto.PurchaseOrderRequest;
import com.erp.inventory.service.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/api/inventory/purchase-orders")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class PurchaseOrderController {

    private final PurchaseOrderService service;

    public PurchaseOrderController(PurchaseOrderService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseOrderDto create(@Valid @RequestBody PurchaseOrderRequest request) {
        return service.create(request);
    }

    @PostMapping("/{id}/receive")
    public PurchaseOrderDto receive(@PathVariable Long id) {
        return service.receive(id);
    }

    @GetMapping
    public Page<PurchaseOrderDto> list(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public PurchaseOrderDto get(@PathVariable Long id) {
        return service.get(id);
    }
}
