package com.erp.sales.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(@NotBlank String name, @Email String email, String phone, String company) {
}
