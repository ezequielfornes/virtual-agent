package com.financial.assistant.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MetricsService}.
 *
 * <p>
 * Verifies counter increments, snapshot structure, recognition rate
 * calculation, and per-intention breakdown tracking.
 * </p>
 */
class MetricsServiceTest {

    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        metricsService = new MetricsService();
    }

    @Nested
    @DisplayName("Counter Operations")
    class CounterTests {

        @Test
        @DisplayName("Should start with all counters at zero")
        void shouldStartWithZeroCounters() {
            final Map<String, Object> snapshot = metricsService.getMetricsSnapshot();

            assertEquals(0L, snapshot.get("total_requests"));
            assertEquals(0L, snapshot.get("recognized_intentions"));
            assertEquals(0L, snapshot.get("unrecognized_intentions"));
            assertEquals(0L, snapshot.get("external_api_calls"));
            assertEquals(0L, snapshot.get("external_api_failures"));
        }

        @Test
        @DisplayName("Should increment total requests")
        void shouldIncrementTotalRequests() {
            metricsService.incrementTotalRequests();
            metricsService.incrementTotalRequests();

            final Map<String, Object> snapshot = metricsService.getMetricsSnapshot();
            assertEquals(2L, snapshot.get("total_requests"));
        }

        @Test
        @DisplayName("Should increment recognized intentions and track by name")
        void shouldIncrementRecognizedIntentions() {
            metricsService.incrementRecognizedIntention("consultar_saldo");
            metricsService.incrementRecognizedIntention("consultar_saldo");
            metricsService.incrementRecognizedIntention("info_producto");

            final Map<String, Object> snapshot = metricsService.getMetricsSnapshot();
            assertEquals(3L, snapshot.get("recognized_intentions"));

            @SuppressWarnings("unchecked")
            final Map<String, Long> breakdown = (Map<String, Long>) snapshot.get("queries_by_intention");
            assertEquals(2L, breakdown.get("consultar_saldo"));
            assertEquals(1L, breakdown.get("info_producto"));
        }

        @Test
        @DisplayName("Should increment unrecognized intentions")
        void shouldIncrementUnrecognizedIntentions() {
            metricsService.incrementUnrecognizedIntentions();

            final Map<String, Object> snapshot = metricsService.getMetricsSnapshot();
            assertEquals(1L, snapshot.get("unrecognized_intentions"));
        }

        @Test
        @DisplayName("Should increment external API calls and failures independently")
        void shouldIncrementExternalApiMetrics() {
            metricsService.incrementExternalApiCalls();
            metricsService.incrementExternalApiCalls();
            metricsService.incrementExternalApiFailures();

            final Map<String, Object> snapshot = metricsService.getMetricsSnapshot();
            assertEquals(2L, snapshot.get("external_api_calls"));
            assertEquals(1L, snapshot.get("external_api_failures"));
        }
    }

    @Nested
    @DisplayName("Recognition Rate")
    class RecognitionRateTests {

        @Test
        @DisplayName("Should return 0% when no requests have been made")
        void shouldReturnZeroRateWhenNoRequests() {
            final Map<String, Object> snapshot = metricsService.getMetricsSnapshot();
            assertEquals(0.0, snapshot.get("intention_recognition_rate_percent"));
        }

        @Test
        @DisplayName("Should calculate correct recognition rate")
        void shouldCalculateCorrectRecognitionRate() {
            metricsService.incrementTotalRequests();
            metricsService.incrementTotalRequests();
            metricsService.incrementTotalRequests();
            metricsService.incrementRecognizedIntention("saldo");
            metricsService.incrementRecognizedIntention("cambio");

            final Map<String, Object> snapshot = metricsService.getMetricsSnapshot();
            // 2/3 = 66.67%
            assertEquals(66.67, snapshot.get("intention_recognition_rate_percent"));
        }

        @Test
        @DisplayName("Should return 100% when all requests are recognized")
        void shouldReturn100PercentWhenAllRecognized() {
            metricsService.incrementTotalRequests();
            metricsService.incrementRecognizedIntention("saldo");

            final Map<String, Object> snapshot = metricsService.getMetricsSnapshot();
            assertEquals(100.0, snapshot.get("intention_recognition_rate_percent"));
        }
    }

    @Test
    @DisplayName("Snapshot should contain all expected keys in order")
    void snapshotShouldContainAllKeysInOrder() {
        final Map<String, Object> snapshot = metricsService.getMetricsSnapshot();

        final var keys = snapshot.keySet().stream().toList();
        assertEquals("total_requests", keys.get(0));
        assertEquals("recognized_intentions", keys.get(1));
        assertEquals("unrecognized_intentions", keys.get(2));
        assertEquals("external_api_calls", keys.get(3));
        assertEquals("external_api_failures", keys.get(4));
        assertEquals("intention_recognition_rate_percent", keys.get(5));
        assertEquals("queries_by_intention", keys.get(6));
    }
}
