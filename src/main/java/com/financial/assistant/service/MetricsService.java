package com.financial.assistant.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks technical and functional metrics for the financial assistant.
 *
 * <p>
 * Provides real-time counters for monitoring system health and
 * understanding user behavior patterns. In a production deployment,
 * these would be exported to Prometheus/Grafana via Micrometer.
 * </p>
 *
 * <p>
 * Key metrics tracked:
 * </p>
 * <ul>
 * <li><strong>Technical:</strong> total requests, external API
 * calls/failures</li>
 * <li><strong>Functional:</strong> queries by intention, recognized vs.
 * unrecognized</li>
 * </ul>
 *
 * <p>
 * The metrics snapshot uses Java 21's {@link java.util.SequencedMap}
 * ({@link LinkedHashMap}) to guarantee a predictable, insertion-ordered
 * output — improving readability for monitoring dashboards and API consumers.
 * </p>
 */
@Service
public class MetricsService {

    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong recognizedIntentions = new AtomicLong(0);
    private final AtomicLong unrecognizedIntentions = new AtomicLong(0);
    private final AtomicLong externalApiCalls = new AtomicLong(0);
    private final AtomicLong externalApiFailures = new AtomicLong(0);
    private final ConcurrentHashMap<String, AtomicLong> intentionCounters = new ConcurrentHashMap<>();

    /** Increments total request count. */
    public void incrementTotalRequests() {
        totalRequests.incrementAndGet();
    }

    /** Records a recognized intention occurrence. */
    public void incrementRecognizedIntention(final String intentionName) {
        recognizedIntentions.incrementAndGet();
        intentionCounters
                .computeIfAbsent(intentionName, k -> new AtomicLong(0))
                .incrementAndGet();
    }

    /** Records an unrecognized intention occurrence. */
    public void incrementUnrecognizedIntentions() {
        unrecognizedIntentions.incrementAndGet();
    }

    /** Records an external API call. */
    public void incrementExternalApiCalls() {
        externalApiCalls.incrementAndGet();
    }

    /** Records an external API failure. */
    public void incrementExternalApiFailures() {
        externalApiFailures.incrementAndGet();
    }

    /**
     * Returns a snapshot of all current metrics.
     *
     * <p>
     * Uses a {@link LinkedHashMap} (a {@link java.util.SequencedMap}
     * since Java 21) to maintain consistent key ordering in the JSON output,
     * improving readability for dashboards and API consumers.
     * </p>
     *
     * @return ordered map of metric names to values
     */
    public Map<String, Object> getMetricsSnapshot() {
        final Map<String, Object> snapshot = new LinkedHashMap<>();

        snapshot.put("total_requests", totalRequests.get());
        snapshot.put("recognized_intentions", recognizedIntentions.get());
        snapshot.put("unrecognized_intentions", unrecognizedIntentions.get());
        snapshot.put("external_api_calls", externalApiCalls.get());
        snapshot.put("external_api_failures", externalApiFailures.get());

        final double successRate = totalRequests.get() > 0
                ? (double) recognizedIntentions.get() / totalRequests.get() * 100.0
                : 0.0;
        snapshot.put("intention_recognition_rate_percent", Math.round(successRate * 100.0) / 100.0);

        final Map<String, Long> intentionBreakdown = new LinkedHashMap<>();
        intentionCounters.forEach((key, value) -> intentionBreakdown.put(key, value.get()));
        snapshot.put("queries_by_intention", intentionBreakdown);

        return snapshot;
    }
}
