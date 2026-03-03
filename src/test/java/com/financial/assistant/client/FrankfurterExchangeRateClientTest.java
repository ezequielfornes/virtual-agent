package com.financial.assistant.client;

import com.financial.assistant.config.ExternalApiProperties;
import com.financial.assistant.service.MetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link FrankfurterExchangeRateClient}.
 *
 * <p>
 * Tests cover successful API calls, fallback behavior on failure,
 * and circuit breaker state transitions. Uses {@code doReturn().when()}
 * pattern to work around RestClient generic type capture limitations.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class FrankfurterExchangeRateClientTest {

    @Mock
    private MetricsService metricsService;

    private RestClient restClient;
    private FrankfurterExchangeRateClient client;

    @BeforeEach
    void setUp() {
        restClient = mock(RestClient.class);

        final ExternalApiProperties properties = new ExternalApiProperties(
                "https://api.frankfurter.dev/v1",
                5000,
                5000,
                new ExternalApiProperties.CircuitBreakerProperties(3, 30000));
        client = new FrankfurterExchangeRateClient(restClient, properties, metricsService);
    }

    private void stubSuccessfulApiCall(final ExchangeRateResponse apiResponse) {
        final var uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        final var headersSpec = mock(RestClient.RequestHeadersSpec.class);
        final var responseSpec = mock(RestClient.ResponseSpec.class);

        doReturn(uriSpec).when(restClient).get();
        doReturn(headersSpec).when(uriSpec).uri(anyString(), any(Object[].class));
        doReturn(responseSpec).when(headersSpec).retrieve();
        doReturn(apiResponse).when(responseSpec).body(ExchangeRateResponse.class);
    }

    private void stubFailedApiCall() {
        final var uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        final var headersSpec = mock(RestClient.RequestHeadersSpec.class);

        doReturn(uriSpec).when(restClient).get();
        doReturn(headersSpec).when(uriSpec).uri(anyString(), any(Object[].class));
        doThrow(new RuntimeException("Connection refused")).when(headersSpec).retrieve();
    }

    @Test
    @DisplayName("Should return exchange rates on successful API call")
    void shouldReturnRatesOnSuccess() {
        final ExchangeRateResponse apiResponse = new ExchangeRateResponse(
                "USD", "2024-01-15",
                Map.of("EUR", 0.9215, "GBP", 0.7893));
        stubSuccessfulApiCall(apiResponse);

        final Map<String, Double> rates = client.getLatestRates("USD");

        assertFalse(rates.isEmpty());
        assertEquals(0.9215, rates.get("EUR"));
        assertEquals(0.7893, rates.get("GBP"));
        verify(metricsService).incrementExternalApiCalls();
    }

    @Test
    @DisplayName("Should return empty map when API call fails and no cache exists")
    void shouldReturnEmptyMapOnFailureWithNoCache() {
        stubFailedApiCall();

        final Map<String, Double> rates = client.getLatestRates("USD");

        assertTrue(rates.isEmpty());
        verify(metricsService).incrementExternalApiCalls();
        verify(metricsService).incrementExternalApiFailures();
    }

    @Test
    @DisplayName("Should return cached rates when API call fails after previous success")
    void shouldReturnCachedRatesOnSubsequentFailure() {
        // First call succeeds
        final ExchangeRateResponse apiResponse = new ExchangeRateResponse(
                "EUR", "2024-01-15", Map.of("USD", 1.0856));
        stubSuccessfulApiCall(apiResponse);
        client.getLatestRates("EUR");

        // Second call fails
        stubFailedApiCall();
        final Map<String, Double> rates = client.getLatestRates("EUR");

        assertFalse(rates.isEmpty());
        assertEquals(1.0856, rates.get("USD"));
    }

    @Test
    @DisplayName("Should open circuit breaker after threshold failures")
    void shouldOpenCircuitBreakerAfterThresholdFailures() {
        stubFailedApiCall();

        // Trigger 3 failures (threshold)
        for (int i = 0; i < 3; i++) {
            client.getLatestRates("USD");
        }

        // 4th call — circuit should be open, API not contacted
        reset(restClient);
        client.getLatestRates("USD");

        // restClient.get() should NOT have been called after reset
        verify(restClient, never()).get();
    }
}
