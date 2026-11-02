package com.erp.accounting.dto;

import com.erp.accounting.domain.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccountRequest(@NotBlank String code, @NotBlank String name, @NotNull AccountType type) {
}
