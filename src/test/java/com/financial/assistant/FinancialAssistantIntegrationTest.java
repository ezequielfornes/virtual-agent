package com.financial.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.assistant.dto.ChatRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Financial Assistant application.
 *
 * <p>
 * Uses {@code @SpringBootTest} to load the full application context
 * with an embedded H2 database, verifying the entire request lifecycle:
 * controller → service → intention resolver → handler → persistence.
 * </p>
 *
 * <p>
 * These tests validate that all layers integrate correctly,
 * including Spring DI wiring of the sealed {@code IntentionHandler}
 * implementations, JPA persistence, and virtual thread configuration.
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FinancialAssistantIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    @DisplayName("Application context should load successfully with Java 21 features")
    void contextLoads() {
        // If context fails to load (e.g., sealed interface wiring issue),
        // this test will fail before reaching the body.
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/v1/chat — balance inquiry should return correct response")
    void shouldHandleBalanceInquiryEndToEnd() throws Exception {
        final ChatRequest request = new ChatRequest("integration-test-customer", "¿Cuál es mi saldo?");

        mockMvc.perform(post("/api/v1/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("integration-test-customer"))
                .andExpect(jsonPath("$.intention").value("consultar_saldo"))
                .andExpect(jsonPath("$.response", containsString("balance")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/v1/chat — product inquiry should return catalog info")
    void shouldHandleProductInquiryEndToEnd() throws Exception {
        final ChatRequest request = new ChatRequest("integration-test-customer",
                "Quiero información sobre tarjeta de crédito");

        mockMvc.perform(post("/api/v1/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intention").value("info_producto"))
                .andExpect(jsonPath("$.response", containsString("Premium Credit Card")));
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/v1/chat — investment inquiry should return recommendation")
    void shouldHandleInvestmentInquiryEndToEnd() throws Exception {
        final ChatRequest request = new ChatRequest("integration-test-customer",
                "Quiero una recomendación de inversión conservador");

        mockMvc.perform(post("/api/v1/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intention").value("recomendacion_inversion"))
                .andExpect(jsonPath("$.response", containsString("Conservative")));
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/v1/chat — unrecognized query should return 422")
    void shouldReturn422ForUnrecognizedIntention() throws Exception {
        final ChatRequest request = new ChatRequest("integration-test-customer",
                "tell me a joke please");

        mockMvc.perform(post("/api/v1/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.correlationId").exists());
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/v1/conversations/{customerId} — should return persisted history")
    void shouldReturnConversationHistory() throws Exception {
        // Conversations from previous test steps should be persisted
        mockMvc.perform(get("/api/v1/conversations/integration-test-customer"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[0].customerId").value("integration-test-customer"));
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/v1/metrics — should reflect accumulated interactions")
    void shouldReturnAccumulatedMetrics() throws Exception {
        mockMvc.perform(get("/api/v1/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_requests", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.recognized_intentions", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.queries_by_intention.consultar_saldo",
                        greaterThanOrEqualTo(1)));
    }

    @Test
    @Order(8)
    @DisplayName("POST /api/v1/chat — validation should reject blank message")
    void shouldReturn400ForBlankMessage() throws Exception {
        mockMvc.perform(post("/api/v1/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"customerId": "123", "message": ""}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
