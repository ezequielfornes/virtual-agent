package com.financial.assistant.controller;

import com.financial.assistant.service.MetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link MetricsController}.
 *
 * <p>
 * Verifies the metrics endpoint returns correct HTTP status
 * and expected JSON structure.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class MetricsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MetricsService metricsService;

    @InjectMocks
    private MetricsController metricsController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(metricsController).build();
    }

    @Test
    @DisplayName("GET /api/v1/metrics should return 200 with metrics snapshot")
    void shouldReturnMetricsSnapshot() throws Exception {
        final Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("total_requests", 10L);
        snapshot.put("recognized_intentions", 8L);
        snapshot.put("unrecognized_intentions", 2L);
        snapshot.put("external_api_calls", 5L);
        snapshot.put("external_api_failures", 1L);
        snapshot.put("intention_recognition_rate_percent", 80.0);
        snapshot.put("queries_by_intention", Map.of("consultar_saldo", 5L));
        when(metricsService.getMetricsSnapshot()).thenReturn(snapshot);

        mockMvc.perform(get("/api/v1/metrics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.total_requests").value(10))
                .andExpect(jsonPath("$.recognized_intentions").value(8))
                .andExpect(jsonPath("$.intention_recognition_rate_percent").value(80.0))
                .andExpect(jsonPath("$.queries_by_intention.consultar_saldo").value(5));
    }
}
