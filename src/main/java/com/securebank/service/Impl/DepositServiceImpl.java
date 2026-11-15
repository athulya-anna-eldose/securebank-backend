
package com.securebank.service.impl;

import com.securebank.dto.DepositRequest;
import com.securebank.dto.DepositResponse;
import com.securebank.entity.Account;
import com.securebank.entity.BankTransaction;
import com.securebank.entity.User;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.TransactionRepository;
import com.securebank.repository.UserRepository;
import com.securebank.service.DepositService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DepositServiceImpl implements DepositService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public DepositServiceImpl(
            UserRepository userRepository,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository) {

        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public DepositResponse depositMoney(
            String userEmail,
            DepositRequest request) {

        // 1. Validate amount
        BigDecimal amount = request.getAmount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Deposit amount must be greater than zero");
        }

        if (amount.scale() > 2) {
            throw new IllegalArgumentException(
                    "Deposit amount cannot have more than 2 decimal places");
        }

        if (amount.precision() - amount.scale() > 13) {
            throw new IllegalArgumentException(
                    "Deposit amount exceeds the allowed limit");
        }

        // 2. Find logged-in user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // 3. Find and lock user's account
        Account userAccount = accountRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Account not found"));

        Account account = accountRepository
                .findByAccountNumberForUpdate(
                        userAccount.getAccountNumber())
                .orElseThrow(() ->
                        new RuntimeException("Account not found"));

        // 4. Update balance
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        // 5. Generate reference number
        String referenceNumber = "SB" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 12)
                        .toUpperCase();

        // 6. Create deposit transaction
        BankTransaction transaction = new BankTransaction();

        transaction.setReferenceNumber(referenceNumber);
        transaction.setType("CREDIT");
        transaction.setAmount(amount);

        transaction.setDescription(
                request.getDescription() == null ||
                        request.getDescription().isBlank()
                        ? "Demo account deposit"
                        : request.getDescription().trim()
        );

        transaction.setStatus("SUCCESS");
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setAccount(account);

        transactionRepository.save(transaction);

        // 7. Return response
        DepositResponse response = new DepositResponse();

        response.setReferenceNumber(referenceNumber);
        response.setMessage("Money added successfully");
        response.setDepositedAmount(amount);
        response.setUpdatedBalance(account.getBalance());

        return response;
    }
}
