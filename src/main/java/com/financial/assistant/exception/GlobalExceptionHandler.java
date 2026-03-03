package com.financial.assistant.exception;

import com.financial.assistant.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Global exception handler for the financial assistant API.
 *
 * <p>
 * Ensures all errors return a consistent {@link ErrorResponse} format with
 * a correlation ID for traceability. Follows financial industry standards by
 * never exposing sensitive internal details in error messages.
 * </p>
 *
 * <p>
 * Leverages Java 21 pattern matching for {@code instanceof} to simplify
 * exception classification and response construction.
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        /**
         * Handles unrecognized financial intentions (HTTP 422).
         */
        @ExceptionHandler(UnrecognizedIntentionException.class)
        public ResponseEntity<ErrorResponse> handleUnrecognizedIntention(final UnrecognizedIntentionException ex) {
                final String correlationId = generateCorrelationId();
                log.warn("Unrecognized intention [correlationId={}]: {}", correlationId, ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                                .body(new ErrorResponse(
                                                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                                                "I couldn't understand your financial query. Please try rephrasing your question "
                                                                + "about account balance, exchange rates, products, or investment recommendations.",
                                                LocalDateTime.now(),
                                                correlationId));
        }

        /**
         * Handles external API failures (HTTP 502).
         */
        @ExceptionHandler(ExternalApiException.class)
        public ResponseEntity<ErrorResponse> handleExternalApiException(final ExternalApiException ex) {
                final String correlationId = generateCorrelationId();
                log.error("External API error [correlationId={}]: {}", correlationId, ex.getMessage(), ex);

                return ResponseEntity
                                .status(HttpStatus.BAD_GATEWAY)
                                .body(new ErrorResponse(
                                                HttpStatus.BAD_GATEWAY.value(),
                                                "We're experiencing issues retrieving financial data. Please try again shortly.",
                                                LocalDateTime.now(),
                                                correlationId));
        }

        /**
         * Handles validation errors from request body (HTTP 400).
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(final MethodArgumentNotValidException ex) {
                final String correlationId = generateCorrelationId();
                final String details = ex.getBindingResult().getFieldErrors().stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .collect(Collectors.joining("; "));

                log.warn("Validation error [correlationId={}]: {}", correlationId, details);

                return ResponseEntity
                                .badRequest()
                                .body(new ErrorResponse(
                                                HttpStatus.BAD_REQUEST.value(),
                                                "Invalid request: " + details,
                                                LocalDateTime.now(),
                                                correlationId));
        }

        /**
         * Catches all unhandled exceptions (HTTP 500).
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGenericException(final Exception ex) {
                final String correlationId = generateCorrelationId();
                log.error("Unexpected error [correlationId={}]: {}", correlationId, ex.getMessage(), ex);

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new ErrorResponse(
                                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                "An internal error occurred. Please contact support with reference: "
                                                                + correlationId,
                                                LocalDateTime.now(),
                                                correlationId));
        }

        private String generateCorrelationId() {
                return UUID.randomUUID().toString();
        }
}
