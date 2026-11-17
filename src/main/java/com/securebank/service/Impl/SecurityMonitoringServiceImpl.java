
package com.securebank.service.Impl;
import com.securebank.entity.SecurityEvent;
import com.securebank.repository.SecurityEventRepository;
import com.securebank.service.SecurityMonitoringService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SecurityMonitoringServiceImpl
        implements SecurityMonitoringService {

    private final SecurityEventRepository securityEventRepository;

    public SecurityMonitoringServiceImpl(
            SecurityEventRepository securityEventRepository) {
        this.securityEventRepository = securityEventRepository;
    }

    @Override
    @Transactional
    public void logEvent(
            String eventType,
            String severity,
            String description,
            String email,
            String ipAddress,
            String endpoint,
            String httpMethod) {

        SecurityEvent event = new SecurityEvent();

        event.setEventType(eventType);
        event.setSeverity(severity);
        event.setDescription(description);
        event.setEmail(email);
        event.setIpAddress(ipAddress);
        event.setEndpoint(endpoint);
        event.setHttpMethod(httpMethod);

        securityEventRepository.save(event);

        // Detect repeated failed login attempts
        if ("LOGIN_FAILURE".equalsIgnoreCase(eventType)
                && ipAddress != null
                && !ipAddress.isBlank()) {

            detectBruteForce(ipAddress);
        }
    }

    private void detectBruteForce(String ipAddress) {

        LocalDateTime fiveMinutesAgo =
                LocalDateTime.now().minusMinutes(5);

        long attempts =
                securityEventRepository
                        .countByEventTypeAndIpAddressAndEventTimeAfter(
                                "LOGIN_FAILURE",
                                ipAddress,
                                fiveMinutesAgo
                        );

        if (attempts == 3) {

            createAlert(
                    ipAddress,
                    "HIGH",
                    "BRUTE_FORCE",
                    "Three failed login attempts detected from IP: "
                            + ipAddress
            );

        } else if (attempts == 5) {

            createAlert(
                    ipAddress,
                    "CRITICAL",
                    "BRUTE_FORCE",
                    "Five failed login attempts detected from IP: "
                            + ipAddress
            );
        }
    }

    private void createAlert(
            String ipAddress,
            String severity,
            String eventType,
            String description) {

        SecurityEvent alert = new SecurityEvent();

        alert.setEventType(eventType);
        alert.setSeverity(severity);
        alert.setDescription(description);
        alert.setIpAddress(ipAddress);
        alert.setEndpoint("/api/auth/login");
        alert.setHttpMethod("POST");

        securityEventRepository.save(alert);
    }

    @Override
    public List<SecurityEvent> getRecentEvents() {
        return securityEventRepository
                .findTop100ByOrderByEventTimeDesc();
    }
    @Override
    public long getXssAttempts() {
        return securityEventRepository
                .countByEventTypeIgnoreCase("XSS_ATTEMPT");
    }
    @Override
    public long getTotalEvents() {
        return securityEventRepository.count();
    }

    @Override
    public long getHighSeverityEvents() {
        return securityEventRepository.countBySeverityIgnoreCase("HIGH")
                + securityEventRepository.countBySeverityIgnoreCase("CRITICAL");
    }
    @Override
    public long getUnauthorizedAccessAttempts() {
        return securityEventRepository
                .countByEventTypeIgnoreCase("UNAUTHORIZED_ACCESS");
    }

    @Override
    public long getInvalidJwtAttempts() {
        return securityEventRepository
                .countByEventTypeIgnoreCase("INVALID_JWT_TOKEN");
    }
    @Override
    public long getSqlInjectionAttempts() {
        return securityEventRepository
                .countByEventTypeIgnoreCase("SQL_INJECTION_ATTEMPT");
    }
}
