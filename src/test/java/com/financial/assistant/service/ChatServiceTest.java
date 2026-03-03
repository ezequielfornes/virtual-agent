package com.financial.assistant.service;

import com.financial.assistant.dto.ChatRequest;
import com.financial.assistant.dto.ChatResponse;
import com.financial.assistant.exception.UnrecognizedIntentionException;
import com.financial.assistant.intention.BalanceIntentionHandler;
import com.financial.assistant.intention.IntentionHandler;
import com.financial.assistant.intention.IntentionResolver;
import com.financial.assistant.model.Conversation;
import com.financial.assistant.repository.ConversationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ChatService}.
 *
 * <p>
 * Tests cover the full conversation pipeline: intention resolution,
 * handler execution, persistence, and error scenarios.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private IntentionResolver intentionResolver;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MetricsService metricsService;

    @Mock
    private BalanceIntentionHandler mockHandler;

    @InjectMocks
    private ChatService chatService;

    private ChatRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new ChatRequest("customer-123", "What is my account balance?");
    }

    @Test
    @DisplayName("Should process message and return response when intention is recognized")
    void shouldProcessMessageSuccessfully() {
        when(intentionResolver.resolve(validRequest.message())).thenReturn(mockHandler);
        when(mockHandler.getIntentionName()).thenReturn("consultar_saldo");
        when(mockHandler.handle(validRequest.message(), validRequest.customerId()))
                .thenReturn("Your balance is 15,420.75 USD");
        when(conversationRepository.save(any(Conversation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final ChatResponse response = chatService.processMessage(validRequest);

        assertNotNull(response);
        assertEquals("customer-123", response.customerId());
        assertEquals("consultar_saldo", response.intention());
        assertEquals("Your balance is 15,420.75 USD", response.response());
        assertNotNull(response.timestamp());

        verify(metricsService).incrementTotalRequests();
        verify(metricsService).incrementRecognizedIntention("consultar_saldo");
    }

    @Test
    @DisplayName("Should persist conversation after processing message")
    void shouldPersistConversationAfterProcessing() {
        when(intentionResolver.resolve(anyString())).thenReturn(mockHandler);
        when(mockHandler.getIntentionName()).thenReturn("consultar_saldo");
        when(mockHandler.handle(anyString(), anyString())).thenReturn("Balance response");
        when(conversationRepository.save(any(Conversation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        chatService.processMessage(validRequest);

        final ArgumentCaptor<Conversation> captor = ArgumentCaptor.forClass(Conversation.class);
        verify(conversationRepository).save(captor.capture());

        final Conversation saved = captor.getValue();
        assertEquals("customer-123", saved.getCustomerId());
        assertEquals("What is my account balance?", saved.getUserMessage());
        assertEquals("Balance response", saved.getAssistantResponse());
        assertEquals("consultar_saldo", saved.getIntention());
    }

    @Test
    @DisplayName("Should throw UnrecognizedIntentionException and increment metrics when intention not found")
    void shouldThrowExceptionWhenIntentionNotRecognized() {
        when(intentionResolver.resolve(anyString()))
                .thenThrow(new UnrecognizedIntentionException("unknown query"));

        assertThrows(UnrecognizedIntentionException.class,
                () -> chatService.processMessage(validRequest));

        verify(metricsService).incrementTotalRequests();
        verify(metricsService).incrementUnrecognizedIntentions();
        verify(conversationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return conversation history for a given customer")
    void shouldReturnConversationHistory() {
        final List<Conversation> expectedHistory = List.of(
                new Conversation("customer-123", "msg1", "resp1", "saldo",
                        java.time.LocalDateTime.now()));
        when(conversationRepository.findByCustomerIdOrderByTimestampDesc("customer-123"))
                .thenReturn(expectedHistory);

        final List<Conversation> history = chatService.getConversationHistory("customer-123");

        assertEquals(1, history.size());
        assertEquals("customer-123", history.get(0).getCustomerId());
    }

    @Test
    @DisplayName("Should increment total requests metric for every message")
    void shouldIncrementTotalRequestsMetric() {
        when(intentionResolver.resolve(anyString())).thenReturn(mockHandler);
        when(mockHandler.getIntentionName()).thenReturn("test");
        when(mockHandler.handle(anyString(), anyString())).thenReturn("response");
        when(conversationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        chatService.processMessage(validRequest);

        verify(metricsService, times(1)).incrementTotalRequests();
    }
}
