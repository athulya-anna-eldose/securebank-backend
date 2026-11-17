package com.securebank.service;

import com.securebank.entity.SecurityEvent;

import java.util.List;

public interface SecurityMonitoringService {

    void logEvent(
            String eventType,
            String severity,
            String description,
            String email,
            String ipAddress,
            String endpoint,
            String httpMethod
    );

    List<SecurityEvent> getRecentEvents();
    long getSqlInjectionAttempts();
    long getTotalEvents();

    long getHighSeverityEvents();
    long getUnauthorizedAccessAttempts();
    long getXssAttempts();
    long getInvalidJwtAttempts();
}