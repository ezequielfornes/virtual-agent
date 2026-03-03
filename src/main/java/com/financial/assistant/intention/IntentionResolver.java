package com.financial.assistant.intention;

import com.financial.assistant.exception.UnrecognizedIntentionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Resolves the user's financial intention by iterating through registered
 * handlers.
 *
 * <p>
 * Uses Spring's dependency injection to automatically discover all
 * {@link IntentionHandler} implementations. Since {@code IntentionHandler}
 * is a sealed interface (Java 21), the compiler guarantees that only the
 * explicitly permitted handlers exist, making the resolution pipeline
 * exhaustive and safe at compile time.
 * </p>
 *
 * <p>
 * Handlers are evaluated in {@link org.springframework.core.annotation.Order}
 * sequence, and the first matching handler is selected.
 * </p>
 *
 * <p>
 * To add a new intention, create a new {@code final} class implementing
 * {@link IntentionHandler}, add it to the {@code permits} clause, and
 * annotate with {@code @Component} — no changes needed here
 * (Open/Closed Principle).
 * </p>
 */
@Component
public class IntentionResolver {

    private static final Logger log = LoggerFactory.getLogger(IntentionResolver.class);

    private final List<IntentionHandler> handlers;

    /**
     * @param handlers all registered intention handlers, injected by Spring
     */
    public IntentionResolver(final List<IntentionHandler> handlers) {
        this.handlers = handlers;
        log.info("Loaded {} intention handlers: {}",
                handlers.size(),
                handlers.stream().map(IntentionHandler::getIntentionName).toList());
    }

    /**
     * Finds the first handler capable of processing the given message.
     *
     * @param message the user's natural-language query
     * @return the matching {@link IntentionHandler}
     * @throws UnrecognizedIntentionException if no handler matches
     */
    public IntentionHandler resolve(final String message) {
        return handlers.stream()
                .filter(handler -> handler.canHandle(message))
                .findFirst()
                .orElseThrow(() -> new UnrecognizedIntentionException(message));
    }
}
