
package com.securebank.service;

import com.securebank.repository.SecurityEventRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class BruteForceProtectionService {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private static final Duration ATTEMPT_WINDOW =
            Duration.ofMinutes(5);

    private static final Duration BLOCK_DURATION =
            Duration.ofMinutes(15);

    private final SecurityEventRepository securityEventRepository;

    private final ConcurrentMap<String, Instant> blockedIps =
            new ConcurrentHashMap<>();

    public BruteForceProtectionService(
            SecurityEventRepository securityEventRepository) {
        this.securityEventRepository = securityEventRepository;
    }

    public boolean isBlocked(String ipAddress) {

        if (ipAddress == null || ipAddress.isBlank()) {
            return false;
        }

        Instant blockedUntil = blockedIps.get(ipAddress);

        if (blockedUntil == null) {
            return false;
        }

        if (Instant.now().isAfter(blockedUntil)) {
            blockedIps.remove(ipAddress);
            return false;
        }

        return true;
    }

    public boolean checkAndBlock(String ipAddress) {

        if (ipAddress == null || ipAddress.isBlank()) {
            return false;
        }

        if (isBlocked(ipAddress)) {
            return true;
        }

        LocalDateTime windowStart =
                LocalDateTime.now().minus(ATTEMPT_WINDOW);

        long failedAttempts =
                securityEventRepository
                        .countByEventTypeAndIpAddressAndEventTimeAfter(
                                "LOGIN_FAILURE",
                                ipAddress,
                                windowStart
                        );

        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {

            blockedIps.put(
                    ipAddress,
                    Instant.now().plus(BLOCK_DURATION)
            );

            return true;
        }

        return false;
    }
}
