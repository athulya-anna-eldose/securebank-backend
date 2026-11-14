package com.securebank.repository;

import com.securebank.entity.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository
        extends JpaRepository<BankTransaction, Long> {

    List<BankTransaction>
    findByAccountIdOrderByTransactionDateDesc(Long accountId);
}