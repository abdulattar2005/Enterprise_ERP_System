package com.erp.inventory.web;

import com.erp.inventory.dto.ProductDto;
import com.erp.inventory.dto.ProductRequest;
import com.erp.inventory.dto.StockAdjustmentRequest;
import com.erp.inventory.service.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ProductDto create(@Valid @RequestBody ProductRequest request) {
        return service.create(request);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public Page<ProductDto> list(@RequestParam(required = false) String category,
                                 @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return service.list(category, pageable);
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('USER')")
    public List<ProductDto> lowStock() {
        return service.lowStock();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ProductDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ProductDto update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/adjust-stock")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ProductDto adjustStock(@PathVariable Long id, @Valid @RequestBody StockAdjustmentRequest request) {
        return service.adjustStock(id, request.delta(), request.reason());
    }
}
