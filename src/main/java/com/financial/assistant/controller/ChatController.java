package com.financial.assistant.controller;

import com.financial.assistant.dto.ChatRequest;
import com.financial.assistant.dto.ChatResponse;
import com.financial.assistant.model.Conversation;
import com.financial.assistant.service.ChatService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for the financial virtual assistant chat API.
 *
 * <p>
 * Exposes the conversational interface for financial queries and
 * provides access to conversation history. All endpoints follow
 * RESTful conventions with proper HTTP status codes.
 * </p>
 *
 * <p>
 * API is versioned under {@code /api/v1/} following Open Banking
 * best practices for API lifecycle management.
 * </p>
 */
@RestController
@RequestMapping("/api/v1")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    public ChatController(final ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Processes a financial conversation message.
     *
     * <p>
     * Accepts a customer query, detects the financial intention,
     * and returns the assistant's response.
     * </p>
     *
     * @param request the chat request containing customerId and message
     * @return HTTP 200 with the assistant's response
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody final ChatRequest request) {
        log.info("Received chat request from customer: {}", request.customerId());
        final ChatResponse response = chatService.processMessage(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the conversation history for a specific customer.
     *
     * @param customerId the customer's identifier
     * @return HTTP 200 with list of past conversations
     */
    @GetMapping("/conversations/{customerId}")
    public ResponseEntity<List<Conversation>> getConversationHistory(
            @PathVariable final String customerId) {
        log.info("Retrieving conversation history for customer: {}", customerId);
        final List<Conversation> history = chatService.getConversationHistory(customerId);
        return ResponseEntity.ok(history);
    }
}
