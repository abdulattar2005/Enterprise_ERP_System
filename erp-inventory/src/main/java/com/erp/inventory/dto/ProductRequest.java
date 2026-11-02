package com.erp.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank String sku,
        @NotBlank String name,
        String category,
        @NotNull @DecimalMin("0.00") BigDecimal unitPrice,
        @Min(0) int stockQuantity,
        @Min(0) int reorderLevel,
        Long warehouseId,
        Long supplierId) {
}
