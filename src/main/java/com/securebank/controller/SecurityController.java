
package com.securebank.controller;

import com.securebank.entity.SecurityEvent;
import com.securebank.service.SecurityMonitoringService;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/security")
public class SecurityController {

    private final SecurityMonitoringService securityMonitoringService;

    public SecurityController(
            SecurityMonitoringService securityMonitoringService) {
        this.securityMonitoringService = securityMonitoringService;
    }

    @GetMapping("/events")
    public List<SecurityEvent> getRecentEvents() {
        return securityMonitoringService.getRecentEvents();
    }

    @GetMapping("/summary")
    public Map<String, Long> getSecuritySummary() {

        Map<String, Long> summary = new HashMap<>();

        summary.put(
                "totalEvents",
                securityMonitoringService.getTotalEvents()
        );

        summary.put(
                "highSeverityEvents",
                securityMonitoringService.getHighSeverityEvents()
        );

        summary.put(
                "unauthorizedAccessAttempts",
                securityMonitoringService.getUnauthorizedAccessAttempts()
        );

        summary.put(
                "invalidJwtAttempts",
                securityMonitoringService.getInvalidJwtAttempts()
        );
        summary.put(
                "sqlInjectionAttempts",
                securityMonitoringService.getSqlInjectionAttempts()
        );
        summary.put(
                "xssAttempts",
                securityMonitoringService.getXssAttempts()
        );

        return summary;
    }

}
