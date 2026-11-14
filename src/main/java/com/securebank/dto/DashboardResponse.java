
package com.securebank.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardResponse {

    private String fullName;
    private String accountNumber;
    private BigDecimal balance;
    private long totalTransactions;
    private List<TransactionResponse> recentTransactions;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public List<TransactionResponse> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(
            List<TransactionResponse> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }
}
