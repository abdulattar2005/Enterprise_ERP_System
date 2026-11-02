package com.erp.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Positive delta adds stock, negative delta removes it. */
public record StockAdjustmentRequest(@NotNull Integer delta, @NotBlank String reason) {
}
