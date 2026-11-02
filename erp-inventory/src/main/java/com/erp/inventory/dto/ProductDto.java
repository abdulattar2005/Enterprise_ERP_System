package com.erp.inventory.dto;

import com.erp.inventory.domain.Product;
import java.math.BigDecimal;

public record ProductDto(Long id, String sku, String name, String category, BigDecimal unitPrice,
                         int stockQuantity, int reorderLevel, Long warehouseId, Long supplierId,
                         boolean active, boolean lowStock) {

    public static ProductDto from(Product p) {
        return new ProductDto(p.getId(), p.getSku(), p.getName(), p.getCategory(), p.getUnitPrice(),
                p.getStockQuantity(), p.getReorderLevel(),
                p.getWarehouse() == null ? null : p.getWarehouse().getId(),
                p.getSupplier() == null ? null : p.getSupplier().getId(),
                p.isActive(), p.getStockQuantity() <= p.getReorderLevel());
    }
}
