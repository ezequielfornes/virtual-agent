package com.financial.assistant.service;

import com.financial.assistant.dto.ChatRequest;
import com.financial.assistant.dto.ChatResponse;
import com.financial.assistant.exception.UnrecognizedIntentionException;
import com.financial.assistant.intention.IntentionHandler;
import com.financial.assistant.intention.IntentionResolver;
import com.financial.assistant.model.Conversation;
import com.financial.assistant.repository.ConversationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Core service orchestrating the financial conversation flow.
 *
 * <p>
 * Coordinates between intention resolution, handler execution, conversation
 * persistence, and metrics recording. This is the central orchestrator of the
 * virtual assistant — all interactions pass through this service.
 * </p>
 *
 * <p>
 * The intention resolution pipeline leverages Java 21 sealed interfaces
 * to guarantee a complete, compiler-verified set of financial intentions.
 * </p>
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final IntentionResolver intentionResolver;
    private final ConversationRepository conversationRepository;
    private final MetricsService metricsService;

    public ChatService(final IntentionResolver intentionResolver,
            final ConversationRepository conversationRepository,
            final MetricsService metricsService) {
        this.intentionResolver = intentionResolver;
        this.conversationRepository = conversationRepository;
        this.metricsService = metricsService;
    }

    /**
     * Processes a customer's chat message through the full pipeline:
     * resolve intention → execute handler → persist → return response.
     *
     * @param request the incoming chat request
     * @return the assistant's response
     */
    public ChatResponse processMessage(final ChatRequest request) {
        log.info("Processing message from customer: {}", request.customerId());
        metricsService.incrementTotalRequests();

        final IntentionHandler handler;
        try {
            handler = intentionResolver.resolve(request.message());
            metricsService.incrementRecognizedIntention(handler.getIntentionName());
        } catch (final UnrecognizedIntentionException ex) {
            metricsService.incrementUnrecognizedIntentions();
            throw ex;
        }

        final String response = handler.handle(request.message(), request.customerId());
        final LocalDateTime timestamp = LocalDateTime.now();

        final Conversation conversation = new Conversation(
                request.customerId(),
                request.message(),
                response,
                handler.getIntentionName(),
                timestamp);
        conversationRepository.save(conversation);

        log.info("Conversation saved for customer {} with intention: {}",
                request.customerId(), handler.getIntentionName());

        return new ChatResponse(
                request.customerId(),
                handler.getIntentionName(),
                response,
                timestamp);
    }

    /**
     * Retrieves the conversation history for a given customer.
     *
     * @param customerId the customer's identifier
     * @return list of past conversations, most recent first
     */
    public List<Conversation> getConversationHistory(final String customerId) {
        return conversationRepository.findByCustomerIdOrderByTimestampDesc(customerId);
    }
}
