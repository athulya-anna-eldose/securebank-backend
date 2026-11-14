
package com.securebank.service.impl;

import com.securebank.dto.DashboardResponse;
import com.securebank.dto.TransactionResponse;
import com.securebank.entity.User;
import com.securebank.entity.Account;
import com.securebank.entity.BankTransaction;
import com.securebank.repository.UserRepository;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.TransactionRepository;
import com.securebank.service.DashboardService;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public DashboardServiceImpl(
            UserRepository userRepository,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public DashboardResponse getDashboard(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Account account = accountRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Account not found"));

        List<BankTransaction> transactions =
                transactionRepository
                        .findByAccountIdOrderByTransactionDateDesc(
                                account.getId());

        List<TransactionResponse> recentTransactions =
                transactions.stream()
                        .limit(5)
                        .map(transaction -> new TransactionResponse(
                                transaction.getId(),
                                transaction.getReferenceNumber(),
                                transaction.getType(),
                                transaction.getAmount(),
                                transaction.getDescription(),
                                transaction.getStatus(),
                                transaction.getTransactionDate()
                        ))
                        .toList();

        DashboardResponse response = new DashboardResponse();

        response.setFullName(user.getFullName());
        response.setAccountNumber(account.getAccountNumber());
        response.setBalance(account.getBalance());
        response.setTotalTransactions(transactions.size());
        response.setRecentTransactions(recentTransactions);

        return response;
    }
}
