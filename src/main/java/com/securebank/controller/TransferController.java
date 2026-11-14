
package com.securebank.controller;

import com.securebank.dto.MoneyTransferRequest;
import com.securebank.dto.TransferResponse;
import com.securebank.service.TransferService;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public TransferResponse transferMoney(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody MoneyTransferRequest request) {

        return transferService.transferMoney(email, request);
    }
}
