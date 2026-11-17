
package com.securebank.config;

import com.securebank.security.ApiRateLimitFilter;
import com.securebank.security.JwtAuthenticationFilter;
import com.securebank.security.SqlInjectionDetectionFilter;
import com.securebank.security.XssDetectionFilter;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final ApiRateLimitFilter apiRateLimitFilter;
    private final SqlInjectionDetectionFilter sqlInjectionFilter;
    private final XssDetectionFilter xssDetectionFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtFilter,
            ApiRateLimitFilter apiRateLimitFilter,
            SqlInjectionDetectionFilter sqlInjectionFilter,
            XssDetectionFilter xssDetectionFilter) {

        this.jwtFilter = jwtFilter;
        this.apiRateLimitFilter = apiRateLimitFilter;
        this.sqlInjectionFilter = sqlInjectionFilter;
        this.xssDetectionFilter = xssDetectionFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173"
        ));

        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    // Prevent rate-limit filter from being registered twice
    @Bean
    public FilterRegistrationBean<ApiRateLimitFilter>
    apiRateLimitFilterRegistration() {

        FilterRegistrationBean<ApiRateLimitFilter> registration =
                new FilterRegistrationBean<>();

        registration.setFilter(apiRateLimitFilter);
        registration.setEnabled(false);

        return registration;
    }

    // Prevent SQL detection filter from being registered twice
    @Bean
    public FilterRegistrationBean<SqlInjectionDetectionFilter>
    sqlInjectionFilterRegistration() {

        FilterRegistrationBean<SqlInjectionDetectionFilter> registration =
                new FilterRegistrationBean<>();

        registration.setFilter(sqlInjectionFilter);
        registration.setEnabled(false);

        return registration;
    }

    // Prevent XSS detection filter from being registered twice
    @Bean
    public FilterRegistrationBean<XssDetectionFilter>
    xssDetectionFilterRegistration() {

        FilterRegistrationBean<XssDetectionFilter> registration =
                new FilterRegistrationBean<>();

        registration.setFilter(xssDetectionFilter);
        registration.setEnabled(false);

        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .cors(Customizer.withDefaults())

                // Security headers
                .headers(headers -> headers

                        .contentTypeOptions(Customizer.withDefaults())

                        .frameOptions(frame -> frame.deny())

                        .referrerPolicy(referrer -> referrer.policy(
                                ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN
                        ))

                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'none'; frame-ancestors 'none';"
                        ))

                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                        )
                )

                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(exception -> exception

                        .authenticationEntryPoint(
                                (request, response, authException) ->
                                        response.sendError(
                                                HttpServletResponse.SC_UNAUTHORIZED,
                                                "Unauthorized: Authentication required"
                                        )
                        )

                        .accessDeniedHandler(
                                (request, response, accessDeniedException) ->
                                        response.sendError(
                                                HttpServletResponse.SC_FORBIDDEN,
                                                "Forbidden: Access denied"
                                        )
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                HttpMethod.OPTIONS, "/**"
                        ).permitAll()

                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()

                        .anyRequest().authenticated()
                )

                // API rate limiting
                .addFilterBefore(
                        apiRateLimitFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                // JWT authentication
                .addFilterAfter(
                        jwtFilter,
                        ApiRateLimitFilter.class
                )

                // SQL injection detection
                .addFilterAfter(
                        sqlInjectionFilter,
                        JwtAuthenticationFilter.class
                )

                // XSS detection
                .addFilterAfter(
                        xssDetectionFilter,
                        SqlInjectionDetectionFilter.class
                );

        return http.build();
    }
}
