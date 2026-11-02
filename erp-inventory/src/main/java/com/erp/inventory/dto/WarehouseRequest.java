package com.erp.inventory.dto;

import jakarta.validation.constraints.NotBlank;

public record WarehouseRequest(@NotBlank String code, @NotBlank String name, String location) {
}
