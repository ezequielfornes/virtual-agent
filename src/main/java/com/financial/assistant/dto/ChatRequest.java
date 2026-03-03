package com.financial.assistant.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Incoming chat request DTO.
 *
 * @param customerId unique identifier for the customer initiating the conversation
 * @param message    the customer's natural-language financial query
 */
public record ChatRequest(
        @NotBlank(message = "Customer ID must not be blank")
        String customerId,

        @NotBlank(message = "Message must not be blank")
        String message
) {
}
