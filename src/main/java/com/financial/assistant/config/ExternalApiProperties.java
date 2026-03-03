package com.financial.assistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalized configuration properties for the Frankfurter exchange rate API.
 *
 * <p>All connection parameters are defined in {@code application.yml} under the
 * {@code external-api.frankfurter} prefix, enabling easy environment-specific overrides.</p>
 */
@ConfigurationProperties(prefix = "external-api.frankfurter")
public record ExternalApiProperties(
        String baseUrl,
        int connectTimeoutMs,
        int readTimeoutMs,
        CircuitBreakerProperties circuitBreaker
) {
    /**
     * Circuit breaker configuration for external API resilience.
     */
    public record CircuitBreakerProperties(
            int failureThreshold,
            long resetTimeoutMs
    ) {}
}
