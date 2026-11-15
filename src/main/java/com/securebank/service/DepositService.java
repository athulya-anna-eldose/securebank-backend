
package com.securebank.service;

import com.securebank.dto.DepositRequest;
import com.securebank.dto.DepositResponse;

public interface DepositService {

    DepositResponse depositMoney(
            String userEmail,
            DepositRequest request
    );
}
