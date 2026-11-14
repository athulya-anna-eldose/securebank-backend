
package com.securebank.service;

import com.securebank.dto.MoneyTransferRequest;
import com.securebank.dto.TransferResponse;

public interface TransferService {

    TransferResponse transferMoney(
            String senderEmail,
            MoneyTransferRequest request);
}
