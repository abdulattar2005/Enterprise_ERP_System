package com.erp.hr.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(
        @NotNull Long employeeId,
        @NotBlank String period,
        @Min(1) @Max(5) int rating,
        String comments) {
}
