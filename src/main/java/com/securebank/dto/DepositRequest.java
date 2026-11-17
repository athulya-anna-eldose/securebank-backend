
package com.securebank.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class DepositRequest {

    @NotNull(message = "Deposit amount is required")
    @DecimalMin(value = "0.01", message = "Deposit amount must be greater than zero")
    @DecimalMax(value = "1000000.00", message = "Maximum deposit amount is 1,000,000")
    private BigDecimal amount;

    @Size(max = 150, message = "Description cannot exceed 150 characters")
    @Pattern(
            regexp = "^[^<>]*$",
            message = "Description must not contain HTML tags"
    )
    private String description;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
