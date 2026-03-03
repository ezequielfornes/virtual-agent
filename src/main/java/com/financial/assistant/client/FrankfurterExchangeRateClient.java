package com.financial.assistant.client;

import com.financial.assistant.config.ExternalApiProperties;
import com.financial.assistant.service.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Frankfurter API implementation of {@link ExchangeRateClient}.
 *
 * <p>
 * Includes a simulated circuit breaker pattern that opens after consecutive
 * failures, preventing cascading issues. Also maintains a simple in-memory
 * cache of the last successful response per currency for fallback.
 * </p>
 *
 * <p>
 * Uses Spring's {@link RestClient} (Spring Boot 3.4) for modern, synchronous
 * HTTP communication. With Java 21 virtual threads enabled, each HTTP call
 * runs on a lightweight virtual thread, allowing thousands of concurrent
 * exchange rate requests without thread pool exhaustion.
 * </p>
 */
@Component
public class FrankfurterExchangeRateClient implements ExchangeRateClient {

    private static final Logger log = LoggerFactory.getLogger(FrankfurterExchangeRateClient.class);

    private final RestClient restClient;
    private final MetricsService metricsService;
    private final int failureThreshold;
    private final long resetTimeoutMs;

    /** Circuit breaker state. */
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0);

    /** Simple in-memory cache for fallback. */
    private final Map<String, Map<String, Double>> rateCache = new ConcurrentHashMap<>();

    public FrankfurterExchangeRateClient(final RestClient restClient,
            final ExternalApiProperties properties,
            final MetricsService metricsService) {
        this.restClient = restClient;
        this.metricsService = metricsService;
        this.failureThreshold = properties.circuitBreaker().failureThreshold();
        this.resetTimeoutMs = properties.circuitBreaker().resetTimeoutMs();
    }

    @Override
    public Map<String, Double> getLatestRates(final String baseCurrency) {
        metricsService.incrementExternalApiCalls();

        if (isCircuitOpen()) {
            log.warn("Circuit breaker is OPEN. Returning cached rates for {}", baseCurrency);
            metricsService.incrementExternalApiFailures();
            return getCachedOrEmpty(baseCurrency);
        }

        try {
            final ExchangeRateResponse response = restClient.get()
                    .uri("/latest?base={base}", baseCurrency)
                    .retrieve()
                    .body(ExchangeRateResponse.class);

            if (response != null && response.rates() != null) {
                consecutiveFailures.set(0);
                rateCache.put(baseCurrency, response.rates());
                log.info("Successfully retrieved exchange rates for {} ({} currencies)",
                        baseCurrency, response.rates().size());
                return response.rates();
            }

            return handleFailure(baseCurrency, new RuntimeException("Empty response from API"));

        } catch (final Exception ex) {
            return handleFailure(baseCurrency, ex);
        }
    }

    /**
     * Records a failure and returns cached or empty rates.
     */
    private Map<String, Double> handleFailure(final String baseCurrency, final Exception ex) {
        final int failures = consecutiveFailures.incrementAndGet();
        lastFailureTime.set(Instant.now().toEpochMilli());
        metricsService.incrementExternalApiFailures();

        log.error("Exchange rate API call failed (failure #{}/{}): {}",
                failures, failureThreshold, ex.getMessage());

        if (failures >= failureThreshold) {
            log.warn("Circuit breaker OPENED after {} consecutive failures", failures);
        }

        return getCachedOrEmpty(baseCurrency);
    }

    /**
     * Checks if the circuit breaker is in the open state.
     */
    private boolean isCircuitOpen() {
        if (consecutiveFailures.get() < failureThreshold) {
            return false;
        }

        final long elapsed = Instant.now().toEpochMilli() - lastFailureTime.get();
        if (elapsed > resetTimeoutMs) {
            log.info("Circuit breaker reset timeout elapsed. Moving to HALF-OPEN state.");
            consecutiveFailures.set(0);
            return false;
        }

        return true;
    }

    /**
     * Returns cached rates or an empty map if no cache exists.
     */
    private Map<String, Double> getCachedOrEmpty(final String baseCurrency) {
        final Map<String, Double> cached = rateCache.get(baseCurrency);
        if (cached != null) {
            log.info("Returning cached exchange rates for {}", baseCurrency);
            return cached;
        }
        return Collections.emptyMap();
    }
}
