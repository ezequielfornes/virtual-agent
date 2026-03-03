package com.financial.assistant.dto;

import java.time.LocalDateTime;

/**
 * Outgoing chat response DTO.
 *
 * @param customerId the customer who initiated the conversation
 * @param intention  the detected financial intention
 * @param response   the assistant's response message
 * @param timestamp  when the response was generated
 */
public record ChatResponse(
        String customerId,
        String intention,
        String response,
        LocalDateTime timestamp
) {
}
