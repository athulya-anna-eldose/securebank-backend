package com.securebank.service;

import com.securebank.dto.TransactionResponse;
import java.util.List;

public interface TransactionService {

    List<TransactionResponse> getTransactions(String email);
}