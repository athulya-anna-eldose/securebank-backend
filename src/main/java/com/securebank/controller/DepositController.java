
package com.securebank.controller;

import com.securebank.dto.DepositRequest;
import com.securebank.dto.DepositResponse;
import com.securebank.service.DepositService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deposits")
public class DepositController {

    private final DepositService depositService;

    public DepositController(DepositService depositService) {
        this.depositService = depositService;
    }

    @PostMapping
    public DepositResponse depositMoney(
            @AuthenticationPrincipal String email,
            @RequestBody DepositRequest request) {

        return depositService.depositMoney(email, request);
    }
}
