
package com.securebank.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class MoneyTransferRequest {

    @NotBlank(message = "Receiver account number is required")
    @Pattern(
            regexp = "^[A-Za-z0-9]{12}$",
            message = "Invalid receiver account number"
    )
    private String toAccountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(
            value = "0.01",
            message = "Amount must be greater than zero"
    )
    @DecimalMax(
            value = "1000000.00",
            message = "Maximum transfer amount is 1,000,000"
    )
    private BigDecimal amount;

    @Size(max = 150, message = "Description cannot exceed 150 characters")
    @Pattern(
            regexp = "^[^<>]*$",
            message = "Description must not contain HTML tags"
    )
    private String description;

    public String getToAccountNumber() {
        return toAccountNumber;
    }

    public void setToAccountNumber(String toAccountNumber) {
        this.toAccountNumber = toAccountNumber;
    }

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
