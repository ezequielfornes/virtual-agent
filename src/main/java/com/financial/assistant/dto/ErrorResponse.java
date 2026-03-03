package com.financial.assistant.dto;

import java.time.LocalDateTime;

/**
 * Standardized error response DTO returned by the global exception handler.
 *
 * <p>Includes a correlation ID for traceability in distributed systems,
 * following financial industry best practices for incident tracking.</p>
 *
 * @param status        HTTP status code
 * @param message       user-friendly error description (never exposes sensitive data)
 * @param timestamp     when the error occurred
 * @param correlationId unique identifier for tracing this error across services
 */
public record ErrorResponse(
        int status,
        String message,
        LocalDateTime timestamp,
        String correlationId
) {
}
