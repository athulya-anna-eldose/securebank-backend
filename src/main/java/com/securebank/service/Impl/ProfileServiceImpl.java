package com.securebank.impl;

import com.securebank.dto.ProfileResponse;
import com.securebank.dto.ProfileUpdateRequest;
import com.securebank.entity.User;
import com.securebank.entity.Account;
import com.securebank.repository.UserRepository;
import com.securebank.repository.AccountRepository;
import com.securebank.service.ProfileService;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public ProfileServiceImpl(
            UserRepository userRepository,
            AccountRepository accountRepository) {

        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public ProfileResponse getProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        Account account = accountRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Account not found")
                );

        return new ProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                account.getAccountNumber()
        );
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(
            String email,
            ProfileUpdateRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone().trim());
        user.setAddress(request.getAddress());

        userRepository.save(user);

        return getProfile(email);
    }

}