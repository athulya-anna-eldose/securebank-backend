
package com.securebank.service.impl;

import com.securebank.dto.MoneyTransferRequest;
import com.securebank.dto.TransferResponse;
import com.securebank.entity.User;
import com.securebank.entity.Account;
import com.securebank.entity.BankTransaction;
import com.securebank.repository.UserRepository;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.TransactionRepository;
import com.securebank.service.TransferService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransferServiceImpl implements TransferService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransferServiceImpl(
            UserRepository userRepository,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository) {

        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public TransferResponse transferMoney(
            String senderEmail,
            MoneyTransferRequest request) {

        // 1. Find sender
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() ->
                        new RuntimeException("Sender not found"));

        // 2. Find sender's account
        Account senderAccount = accountRepository
                .findByAccountNumberForUpdate(
                        accountRepository.findByUserId(sender.getId())
                                .orElseThrow(() ->
                                        new RuntimeException("Sender account not found"))
                                .getAccountNumber())
                .orElseThrow(() ->
                        new RuntimeException("Sender account not found"));

        // 3. Find and lock receiver's account
        Account receiverAccount = accountRepository
                .findByAccountNumberForUpdate(
                        request.getToAccountNumber())
                .orElseThrow(() ->
                        new RuntimeException("Receiver account not found"));

        // 4. Prevent transfer to own account
        if (senderAccount.getId().equals(receiverAccount.getId())) {
            throw new IllegalArgumentException(
                    "You cannot transfer money to your own account");
        }

        BigDecimal amount = request.getAmount();

        // 5. Check amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Transfer amount must be greater than zero");
        }

        // 6. Check available balance
        if (senderAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient account balance");
        }

        // 7. Update balances
        senderAccount.setBalance(
                senderAccount.getBalance().subtract(amount));

        receiverAccount.setBalance(
                receiverAccount.getBalance().add(amount));

        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);

        // 8. Generate transaction reference
        String referenceNumber = "SB" +
                UUID.randomUUID().toString()
                        .replace("-", "")
                        .substring(0, 12).toUpperCase();

        String description = request.getDescription() == null ||
                request.getDescription().isBlank()
                ? "Money transfer"
                : request.getDescription().trim();

        // 9. Create sender's debit transaction
        BankTransaction debit = new BankTransaction();

        debit.setReferenceNumber(referenceNumber);
        debit.setType("DEBIT");
        debit.setAmount(amount);
        debit.setDescription(
                "Transfer to " + receiverAccount.getAccountNumber()
                        + " - " + description);
        debit.setStatus("SUCCESS");
        debit.setTransactionDate(LocalDateTime.now());
        debit.setAccount(senderAccount);

        // 10. Create receiver's credit transaction
        BankTransaction credit = new BankTransaction();

        credit.setReferenceNumber(referenceNumber);
        credit.setType("CREDIT");
        credit.setAmount(amount);
        credit.setDescription(
                "Transfer from " + senderAccount.getAccountNumber()
                        + " - " + description);
        credit.setStatus("SUCCESS");
        credit.setTransactionDate(LocalDateTime.now());
        credit.setAccount(receiverAccount);

        transactionRepository.save(debit);
        transactionRepository.save(credit);

        // 11. Return response
        TransferResponse response = new TransferResponse();

        response.setReferenceNumber(referenceNumber);
        response.setMessage("Money transferred successfully");
        response.setTransferredAmount(amount);
        response.setRemainingBalance(senderAccount.getBalance());

        return response;
    }
}
