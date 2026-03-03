package com.financial.assistant.exception;

/**
 * Thrown when the user's message does not match any known financial intention.
 *
 * <p>
 * Results in an HTTP 422 Unprocessable Entity response, guiding the user
 * to rephrase their query.
 * </p>
 */
public class UnrecognizedIntentionException extends RuntimeException {

    /**
     * @param message the user's original message that could not be classified
     */
    public UnrecognizedIntentionException(final String message) {
        super("Unable to recognize financial intention from message: " + message);
    }
}
