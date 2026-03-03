package com.financial.assistant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.financial.assistant.dto.ChatRequest;
import com.financial.assistant.dto.ChatResponse;
import com.financial.assistant.exception.GlobalExceptionHandler;
import com.financial.assistant.exception.UnrecognizedIntentionException;
import com.financial.assistant.service.ChatService;
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

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link ChatController} using standalone MockMvc setup.
 *
 * <p>
 * Verifies REST endpoint behavior including request validation,
 * successful responses, and error handling via the global exception handler.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

        private MockMvc mockMvc;

        private ObjectMapper objectMapper;

        @Mock
        private ChatService chatService;

        @InjectMocks
        private ChatController chatController;

        @BeforeEach
        void setUp() {
                objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());

                mockMvc = MockMvcBuilders.standaloneSetup(chatController)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();
        }

        @Test
        @DisplayName("POST /api/v1/chat should return 200 with valid request")
        void shouldReturnOkForValidChatRequest() throws Exception {
                final ChatResponse response = new ChatResponse(
                                "123", "consultar_saldo", "Your balance is 15,420.75 USD",
                                LocalDateTime.now());
                when(chatService.processMessage(any(ChatRequest.class))).thenReturn(response);

                mockMvc.perform(post("/api/v1/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "customerId": "123",
                                                    "message": "What is my balance?"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.customerId").value("123"))
                                .andExpect(jsonPath("$.intention").value("consultar_saldo"))
                                .andExpect(jsonPath("$.response").value("Your balance is 15,420.75 USD"));
        }

        @Test
        @DisplayName("POST /api/v1/chat should return 422 when intention is not recognized")
        void shouldReturnUnprocessableEntityWhenIntentionNotRecognized() throws Exception {
                when(chatService.processMessage(any(ChatRequest.class)))
                                .thenThrow(new UnrecognizedIntentionException("random gibberish"));

                mockMvc.perform(post("/api/v1/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "customerId": "123",
                                                    "message": "random gibberish"
                                                }
                                                """))
                                .andExpect(status().isUnprocessableEntity())
                                .andExpect(jsonPath("$.status").value(422))
                                .andExpect(jsonPath("$.correlationId").exists());
        }

        @Test
        @DisplayName("POST /api/v1/chat should return 500 when unexpected error occurs")
        void shouldReturnInternalServerErrorOnUnexpectedException() throws Exception {
                when(chatService.processMessage(any(ChatRequest.class)))
                                .thenThrow(new RuntimeException("Database connection lost"));

                mockMvc.perform(post("/api/v1/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "customerId": "123",
                                                    "message": "What is my balance?"
                                                }
                                                """))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.status").value(500))
                                .andExpect(jsonPath("$.correlationId").exists());
        }

        @Test
        @DisplayName("GET /api/v1/conversations/{customerId} should return 200 with history")
        void shouldReturnConversationHistory() throws Exception {
                when(chatService.getConversationHistory("123"))
                                .thenReturn(Collections.emptyList());

                mockMvc.perform(get("/api/v1/conversations/123"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("POST /api/v1/chat should return 400 when message is blank")
        void shouldReturnBadRequestWhenMessageIsBlank() throws Exception {
                mockMvc.perform(post("/api/v1/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "customerId": "123",
                                                    "message": ""
                                                }
                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("POST /api/v1/chat should return 400 when customerId is blank")
        void shouldReturnBadRequestWhenCustomerIdIsBlank() throws Exception {
                mockMvc.perform(post("/api/v1/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "customerId": "",
                                                    "message": "What is my balance?"
                                                }
                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400));
        }
}
