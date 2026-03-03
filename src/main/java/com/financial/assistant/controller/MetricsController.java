package com.financial.assistant.controller;

import com.financial.assistant.service.MetricsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller exposing application metrics.
 *
 * <p>
 * Provides a JSON snapshot of technical and functional metrics
 * for monitoring dashboards and alerting systems. In a production
 * setup, these would be complemented by Prometheus-compatible
 * endpoints via Micrometer/Actuator.
 * </p>
 */
@RestController
@RequestMapping("/api/v1")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(final MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Returns a snapshot of current application metrics.
     *
     * @return HTTP 200 with metrics JSON
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        return ResponseEntity.ok(metricsService.getMetricsSnapshot());
    }
}
