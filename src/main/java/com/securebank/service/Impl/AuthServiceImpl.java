
package com.securebank.service.Impl;

import com.securebank.dto.*;
import com.securebank.entity.Account;
import com.securebank.entity.User;
import com.securebank.repository.AccountRepository;
import com.securebank.repository.UserRepository;
import com.securebank.security.JwtService;
import com.securebank.service.AuthService;
import com.securebank.service.SecurityMonitoringService;
import com.securebank.service.BruteForceProtectionService;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecurityMonitoringService securityMonitoringService;
    private final BruteForceProtectionService bruteForceProtectionService;

    public AuthServiceImpl(
            UserRepository userRepository,
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            SecurityMonitoringService securityMonitoringService,
            BruteForceProtectionService bruteForceProtectionService) {

        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.securityMonitoringService = securityMonitoringService;
        this.bruteForceProtectionService = bruteForceProtectionService;
    }

    @Override
    @Transactional
    public String register(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(email);
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        Account account = new Account();

        account.setUser(savedUser);
        account.setBalance(BigDecimal.ZERO);

        account.setAccountNumber(
                UUID.randomUUID().toString()
                        .replace("-", "")
                        .substring(0, 12)
                        .toUpperCase()
        );

        accountRepository.save(account);

        return "Registration successful";
    }

    @Override
    public LoginResponse login(LoginRequest request, String ipAddress) {

        String email = request.getEmail().trim().toLowerCase();

        // STEP 1: Check whether the IP is blocked
        if (bruteForceProtectionService.checkAndBlock(ipAddress)) {

            securityMonitoringService.logEvent(
                    "LOGIN_BLOCKED",
                    "HIGH",
                    "Login blocked due to repeated failed attempts",
                    email,
                    ipAddress,
                    "/api/auth/login",
                    "POST"
            );

            throw new BadCredentialsException(
                    "Too many failed login attempts. " +
                            "This IP is temporarily blocked. Try again after 15 minutes."
            );
        }

        // STEP 2: Verify user credentials
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null ||
                !passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword())) {

            securityMonitoringService.logEvent(
                    "LOGIN_FAILURE",
                    "MEDIUM",
                    "Failed login attempt",
                    email,
                    ipAddress,
                    "/api/auth/login",
                    "POST"
            );

            throw new BadCredentialsException(
                    "Invalid email or password"
            );
        }

        // STEP 3: Generate JWT token
        String token = jwtService.generateToken(user.getEmail());

        // STEP 4: Record successful login
        securityMonitoringService.logEvent(
                "LOGIN_SUCCESS",
                "LOW",
                "User logged in successfully",
                email,
                ipAddress,
                "/api/auth/login",
                "POST"
        );

        return new LoginResponse(
                token,
                "Login successful",
                user.getFullName()
        );
    }
}
