
package com.securebank.security;

import com.securebank.service.SecurityMonitoringService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Component
public class XssDetectionFilter extends OncePerRequestFilter {

    private final SecurityMonitoringService securityMonitoringService;

    private static final Pattern[] XSS_PATTERNS = {

            // Script tags
            Pattern.compile(
                    "<\\s*script\\b",
                    Pattern.CASE_INSENSITIVE
            ),

            // JavaScript event handlers
            Pattern.compile(
                    "\\bon\\w+\\s*=",
                    Pattern.CASE_INSENSITIVE
            ),

            // JavaScript protocol
            Pattern.compile(
                    "javascript\\s*:",
                    Pattern.CASE_INSENSITIVE
            ),

            // HTML iframe injection
            Pattern.compile(
                    "<\\s*iframe\\b",
                    Pattern.CASE_INSENSITIVE
            ),

            // Dangerous HTML elements
            Pattern.compile(
                    "<\\s*(object|embed|svg)\\b",
                    Pattern.CASE_INSENSITIVE
            ),

            // JavaScript execution functions
            Pattern.compile(
                    "\\b(eval|alert|prompt|confirm)\\s*\\(",
                    Pattern.CASE_INSENSITIVE
            ),

            // HTML data URI
            Pattern.compile(
                    "data\\s*:\\s*text/html",
                    Pattern.CASE_INSENSITIVE
            )
    };

    public XssDetectionFilter(
            SecurityMonitoringService securityMonitoringService
    ) {
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
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestBody = "";
        HttpServletRequest requestToUse = request;

        // Preserve small request bodies for controller processing.
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

        // Inspect request body.
        requestData.append(requestBody);

        String data = requestData.toString();

        boolean suspiciousRequest = false;

        for (Pattern pattern : XSS_PATTERNS) {

            if (pattern.matcher(data).find()) {
                suspiciousRequest = true;
                break;
            }
        }

        if (suspiciousRequest) {

            securityMonitoringService.logEvent(
                    "XSS_ATTEMPT",
                    "HIGH",
                    "Suspicious XSS pattern detected in API request",
                    null,
                    request.getRemoteAddr(),
                    request.getRequestURI(),
                    request.getMethod()
            );
        }

        // Detection only: continue processing the request.
        filterChain.doFilter(requestToUse, response);
    }

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
        public jakarta.servlet.ServletInputStream getInputStream() {

            ByteArrayInputStream inputStream =
                    new ByteArrayInputStream(cachedBody);

            return new jakarta.servlet.ServletInputStream() {

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
                public void setReadListener(
                        jakarta.servlet.ReadListener readListener
                ) {
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
