package com.erp.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record PurchaseOrderRequest(@NotNull Long supplierId, @NotEmpty @Valid List<Line> lines) {

    public record Line(@NotNull Long productId, @Min(1) int quantity, @NotNull @DecimalMin("0.00") BigDecimal unitCost) {
    }
}
