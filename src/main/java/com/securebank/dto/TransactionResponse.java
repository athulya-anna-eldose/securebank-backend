package com.securebank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private Long id;
    private String referenceNumber;
    private String type;
    private BigDecimal amount;
    private String description;
    private String status;
    private LocalDateTime transactionDate;

    public TransactionResponse(Long id, String referenceNumber,
                               String type, BigDecimal amount,
                               String description, String status,
                               LocalDateTime transactionDate) {
        this.id = id;
        this.referenceNumber = referenceNumber;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.status = status;
        this.transactionDate = transactionDate;
    }

    public Long getId() { return id; }
    public String getReferenceNumber() { return referenceNumber; }
    public String getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
}