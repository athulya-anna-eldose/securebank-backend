
package com.securebank.security;

import com.securebank.service.SecurityMonitoringService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final SecurityMonitoringService securityMonitoringService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            SecurityMonitoringService securityMonitoringService) {

        this.jwtService = jwtService;
        this.securityMonitoringService = securityMonitoringService;
    }

    // Skip public authentication endpoints and CORS preflight requests
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        return request.getMethod().equalsIgnoreCase("OPTIONS")
                || request.getRequestURI().startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        String ipAddress = request.getRemoteAddr();
        String endpoint = request.getRequestURI();
        String method = request.getMethod();

        // Case 1: Missing or incorrectly formatted Authorization header
        if (header == null || !header.startsWith("Bearer ")) {

            securityMonitoringService.logEvent(
                    "UNAUTHORIZED_ACCESS",
                    "MEDIUM",
                    "Unauthenticated access attempt to protected API",
                    null,
                    ipAddress,
                    endpoint,
                    method
            );

            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {

            // Case 2: Validate JWT token
            if (jwtService.isTokenValid(token)) {

                String email = jwtService.extractEmail(token);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                Collections.emptyList()
                        );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);

            } else {

                // Case 3: Invalid JWT token
                securityMonitoringService.logEvent(
                        "INVALID_JWT_TOKEN",
                        "HIGH",
                        "Invalid or expired JWT token used to access protected API",
                        null,
                        ipAddress,
                        endpoint,
                        method
                );

                SecurityContextHolder.clearContext();
            }

        } catch (RuntimeException exception) {

            // Case 4: Token validation or extraction error
            securityMonitoringService.logEvent(
                    "INVALID_JWT_TOKEN",
                    "HIGH",
                    "JWT validation failed while accessing protected API",
                    null,
                    ipAddress,
                    endpoint,
                    method
            );

            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
