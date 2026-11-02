package com.erp.inventory.service;

import com.erp.core.audit.AuditService;
import com.erp.core.exception.BusinessRuleException;
import com.erp.core.exception.ResourceNotFoundException;
import com.erp.inventory.domain.Product;
import com.erp.inventory.dto.ProductDto;
import com.erp.inventory.dto.ProductRequest;
import com.erp.inventory.repository.ProductRepository;
import com.erp.inventory.repository.SupplierRepository;
import com.erp.inventory.repository.WarehouseRepository;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository products;
    private final WarehouseRepository warehouses;
    private final SupplierRepository suppliers;
    private final AuditService audit;

    public ProductService(ProductRepository products, WarehouseRepository warehouses, SupplierRepository suppliers,
                          AuditService audit) {
        this.products = products;
        this.warehouses = warehouses;
        this.suppliers = suppliers;
        this.audit = audit;
    }

    public ProductDto create(ProductRequest request) {
        if (products.existsBySkuIgnoreCase(request.sku())) {
            throw new BusinessRuleException("A product with this SKU already exists: " + request.sku());
        }
        Product product = new Product();
        apply(product, request);
        product.setStockQuantity(request.stockQuantity());
        Product saved = products.save(product);
        audit.log("CREATE_PRODUCT", "Created product " + saved.getSku() + " (id=" + saved.getId() + ")");
        return ProductDto.from(saved);
    }

    /** L1 cache (Caffeine): product reads are far more frequent than writes. */
    @Cacheable(value = "products", key = "#id")
    @Transactional(readOnly = true)
    public ProductDto get(Long id) {
        return ProductDto.from(find(id));
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> list(String category, Pageable pageable) {
        Page<Product> page = (category == null || category.isBlank())
                ? products.findAll(pageable)
                : products.findByCategoryIgnoreCase(category, pageable);
        return page.map(ProductDto::from);
    }

    /** Stock level is changed only through {@link #adjustStock}, never through update. */
    @CacheEvict(value = "products", key = "#id")
    public ProductDto update(Long id, ProductRequest request) {
        Product product = find(id);
        if (!product.getSku().equalsIgnoreCase(request.sku()) && products.existsBySkuIgnoreCase(request.sku())) {
            throw new BusinessRuleException("A product with this SKU already exists: " + request.sku());
        }
        apply(product, request);
        audit.log("UPDATE_PRODUCT", "Updated product id=" + id);
        return ProductDto.from(product);
    }

    /**
     * Single entry point for every stock movement (manual adjustment, purchase receipt, sale).
     * The Product row carries a @Version column, so two concurrent adjustments of the same product
     * cannot silently overwrite each other: the loser gets an optimistic-lock failure (HTTP 409).
     */
    @CacheEvict(value = "products", key = "#id")
    public ProductDto adjustStock(Long id, int delta, String reason) {
        if (delta == 0) {
            throw new BusinessRuleException("Stock adjustment must not be zero");
        }
        Product product = find(id);
        int updated = product.getStockQuantity() + delta;
        if (updated < 0) {
            throw new InsufficientStockException(product.getSku(), product.getStockQuantity(), -delta);
        }
        product.setStockQuantity(updated);
        Product saved = products.saveAndFlush(product);
        audit.log("STOCK_ADJUST", product.getSku() + " " + (delta > 0 ? "+" : "") + delta + " (" + reason + ")");
        return ProductDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> lowStock() {
        return products.findLowStock().stream().map(ProductDto::from).toList();
    }

    private Product find(Long id) {
        return products.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Product", id));
    }

    private void apply(Product p, ProductRequest r) {
        p.setSku(r.sku().trim());
        p.setName(r.name().trim());
        p.setCategory(r.category());
        p.setUnitPrice(r.unitPrice());
        p.setReorderLevel(r.reorderLevel());
        p.setWarehouse(r.warehouseId() == null ? null : warehouses.findById(r.warehouseId())
                .orElseThrow(() -> ResourceNotFoundException.of("Warehouse", r.warehouseId())));
        p.setSupplier(r.supplierId() == null ? null : suppliers.findById(r.supplierId())
                .orElseThrow(() -> ResourceNotFoundException.of("Supplier", r.supplierId())));
    }
}
