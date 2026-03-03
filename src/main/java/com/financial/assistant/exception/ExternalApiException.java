package com.financial.assistant.exception;

/**
 * Thrown when communication with an external financial API fails.
 *
 * <p>
 * Wraps the underlying cause to provide context-specific error messages
 * without exposing internal implementation details.
 * </p>
 */
public class ExternalApiException extends RuntimeException {

    /**
     * @param message descriptive error message
     * @param cause   the original exception
     */
    public ExternalApiException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message descriptive error message
     */
    public ExternalApiException(final String message) {
        super(message);
    }
}
