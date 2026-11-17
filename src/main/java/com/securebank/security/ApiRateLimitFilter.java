
package com.securebank.security;

import com.securebank.service.SecurityMonitoringService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ApiRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 60;

    private static final long WINDOW_MILLIS = 60_000;

    private final ConcurrentMap<String, RateLimitBucket> requestCounts =
            new ConcurrentHashMap<>();

    private final AtomicLong totalRequests = new AtomicLong(0);

    private final SecurityMonitoringService securityMonitoringService;

    public ApiRateLimitFilter(
            SecurityMonitoringService securityMonitoringService) {
        this.securityMonitoringService = securityMonitoringService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Apply rate limiting only to API endpoints
        if (!requestPath.startsWith("/api/") ||
                "OPTIONS".equalsIgnoreCase(request.getMethod())) {

            filterChain.doFilter(request, response);
            return;
        }

        String ipAddress = request.getRemoteAddr();

        if (ipAddress == null || ipAddress.isBlank()) {
            ipAddress = "UNKNOWN";
        }

        long currentTime = System.currentTimeMillis();

        RateLimitBucket bucket = requestCounts.computeIfAbsent(
                ipAddress,
                key -> new RateLimitBucket(currentTime)
        );

        boolean allowed;

        synchronized (bucket) {

            // Reset the counter after one minute
            if (currentTime - bucket.windowStart >= WINDOW_MILLIS) {

                bucket.windowStart = currentTime;
                bucket.requestCount = 0;
                bucket.alertLogged = false;
            }

            bucket.requestCount++;

            allowed = bucket.requestCount <= MAX_REQUESTS;
        }

        // Periodically remove expired IP entries
        if (totalRequests.incrementAndGet() % 1000 == 0) {

            requestCounts.entrySet().removeIf(entry -> {

                RateLimitBucket currentBucket = entry.getValue();

                synchronized (currentBucket) {
                    return currentTime - currentBucket.windowStart
                            >= WINDOW_MILLIS * 2;
                }
            });
        }

        if (!allowed) {

            boolean shouldLog;

            synchronized (bucket) {
                shouldLog = !bucket.alertLogged;
                bucket.alertLogged = true;
            }

            // Record only one alert per IP per rate-limit window
            if (shouldLog) {

                try {
                    securityMonitoringService.logEvent(
                            "API_RATE_LIMIT",
                            "HIGH",
                            "API rate limit exceeded by IP: " + ipAddress,
                            null,
                            ipAddress,
                            requestPath,
                            request.getMethod()
                    );
                } catch (RuntimeException ignored) {
                    // Continue blocking even if event logging fails
                }
            }

            response.setStatus(429);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(
                    "{\"message\":\"Too many API requests. " +
                            "Please wait one minute and try again.\"}"
            );

            return;
        }

        filterChain.doFilter(request, response);
    }

    private static class RateLimitBucket {

        private long windowStart;
        private int requestCount;
        private boolean alertLogged;

        private RateLimitBucket(long windowStart) {
            this.windowStart = windowStart;
            this.requestCount = 0;
            this.alertLogged = false;
        }
    }
}
