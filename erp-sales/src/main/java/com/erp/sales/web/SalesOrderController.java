package com.erp.sales.web;

import com.erp.sales.dto.SalesOrderDto;
import com.erp.sales.dto.SalesOrderRequest;
import com.erp.sales.service.SalesOrderService;
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
@RequestMapping("/api/sales/orders")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class SalesOrderController {

    private final SalesOrderService service;

    public SalesOrderController(SalesOrderService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SalesOrderDto create(@Valid @RequestBody SalesOrderRequest request) {
        return service.create(request);
    }

    @PostMapping("/{id}/confirm")
    public SalesOrderDto confirm(@PathVariable Long id) {
        return service.confirm(id);
    }

    @PostMapping("/{id}/cancel")
    public SalesOrderDto cancel(@PathVariable Long id) {
        return service.cancel(id);
    }

    @GetMapping
    public Page<SalesOrderDto> list(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public SalesOrderDto get(@PathVariable Long id) {
        return service.get(id);
    }
}
