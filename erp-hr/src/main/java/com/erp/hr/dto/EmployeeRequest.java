package com.erp.hr.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        String department,
        String jobTitle,
        @NotNull @DecimalMin("0.00") BigDecimal monthlySalary,
        @NotNull @PastOrPresent LocalDate hireDate) {
}
