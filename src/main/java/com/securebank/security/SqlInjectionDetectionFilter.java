
package com.securebank.security;

import com.securebank.service.SecurityMonitoringService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ReadListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Component
public class SqlInjectionDetectionFilter extends OncePerRequestFilter {

    private final SecurityMonitoringService securityMonitoringService;

    private static final Pattern[] SQL_INJECTION_PATTERNS = {

            // Classic SQL injection: ' OR '1'='1
            Pattern.compile(
                    "(['\"]\\s*OR\\s+['\"\\d]+\\s*=\\s*['\"\\d]+)",
                    Pattern.CASE_INSENSITIVE
            ),

            // UNION SELECT attacks
            Pattern.compile(
                    "\\bUNION\\s+(ALL\\s+)?SELECT\\b",
                    Pattern.CASE_INSENSITIVE
            ),

            // SQL command injection
            Pattern.compile(
                    ";\\s*(DROP|DELETE|UPDATE|INSERT|ALTER|TRUNCATE)\\b",
                    Pattern.CASE_INSENSITIVE
            ),

            // Database information discovery
            Pattern.compile(
                    "\\bINFORMATION_SCHEMA\\b",
                    Pattern.CASE_INSENSITIVE
            ),

            // SQL time-based injection
            Pattern.compile(
                    "\\b(SLEEP|BENCHMARK)\\s*\\(",
                    Pattern.CASE_INSENSITIVE
            ),

            // SQL comment-based injection
            Pattern.compile(
                    "(--\\s|/\\*|\\*/|#\\s)",
                    Pattern.CASE_INSENSITIVE
            )
    };

    public SqlInjectionDetectionFilter(
            SecurityMonitoringService securityMonitoringService) {

        this.securityMonitoringService = securityMonitoringService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        return request.getMethod().equalsIgnoreCase("OPTIONS")
                || !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String requestBody = "";

        HttpServletRequest requestToUse = request;

        long contentLength = request.getContentLengthLong();

        if (contentLength >= 0 && contentLength <= 1024 * 1024) {

            CachedBodyRequestWrapper wrappedRequest =
                    new CachedBodyRequestWrapper(request);

            requestToUse = wrappedRequest;

            requestBody = new String(
                    wrappedRequest.getCachedBody(),
                    StandardCharsets.UTF_8
            );
        }

        StringBuilder requestData = new StringBuilder();

        // Inspect URL query parameters.
        if (request.getQueryString() != null) {
            requestData.append(request.getQueryString()).append(" ");
        }

        // Inspect request parameters.
        requestToUse.getParameterMap().forEach((key, values) -> {

            requestData.append(key).append(" ");

            for (String value : values) {
                requestData.append(value).append(" ");
            }
        });

        // Inspect JSON or other request body content.
        requestData.append(requestBody);

        String data = requestData.toString();

        boolean suspiciousRequest = false;

        for (Pattern pattern : SQL_INJECTION_PATTERNS) {

            if (pattern.matcher(data).find()) {
                suspiciousRequest = true;
                break;
            }
        }

        if (suspiciousRequest) {

            securityMonitoringService.logEvent(
                    "SQL_INJECTION_ATTEMPT",
                    "HIGH",
                    "Suspicious SQL injection pattern detected in API request",
                    null,
                    request.getRemoteAddr(),
                    request.getRequestURI(),
                    request.getMethod()
            );
        }

        // Detection only: allow the request to continue.
        filterChain.doFilter(requestToUse, response);
    }

    // Wrapper that allows the request body to be read more than once.
    private static class CachedBodyRequestWrapper
            extends HttpServletRequestWrapper {

        private final byte[] cachedBody;

        public CachedBodyRequestWrapper(HttpServletRequest request)
                throws IOException {

            super(request);
            this.cachedBody = request.getInputStream().readAllBytes();
        }

        public byte[] getCachedBody() {
            return cachedBody;
        }

        @Override
        public ServletInputStream getInputStream() {

            ByteArrayInputStream inputStream =
                    new ByteArrayInputStream(cachedBody);

            return new ServletInputStream() {

                @Override
                public int read() {
                    return inputStream.read();
                }

                @Override
                public boolean isFinished() {
                    return inputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    throw new UnsupportedOperationException(
                            "Non-blocking input is not supported"
                    );
                }
            };
        }

        @Override
        public BufferedReader getReader() {

            return new BufferedReader(
                    new InputStreamReader(
                            getInputStream(),
                            StandardCharsets.UTF_8
                    )
            );
        }

        @Override
        public int getContentLength() {
            return cachedBody.length;
        }

        @Override
        public long getContentLengthLong() {
            return cachedBody.length;
        }
    }
}
