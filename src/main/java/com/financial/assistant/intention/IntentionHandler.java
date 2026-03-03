package com.financial.assistant.intention;

/**
 * Sealed strategy interface for handling financial conversation intentions.
 *
 * <p>
 * Each permitted implementation handles a specific type of financial query
 * (e.g., balance inquiry, exchange rate lookup). The {@code sealed} modifier
 * (Java 21) makes the set of known intentions explicit and compiler-enforced,
 * enabling exhaustive pattern matching in {@code switch} expressions.
 * </p>
 *
 * <p>
 * New intentions can be added by creating a new {@code final} class
 * implementing this interface and adding it to the {@code permits} clause —
 * following the Open/Closed Principle — without modifying the existing
 * resolution pipeline.
 * </p>
 *
 * <p>
 * Register implementations as Spring beans and they will be
 * automatically discovered by {@link IntentionResolver}.
 * </p>
 *
 * @see BalanceIntentionHandler
 * @see ExchangeRateIntentionHandler
 * @see ProductInfoIntentionHandler
 * @see InvestmentRecommendationHandler
 */
public sealed
interface IntentionHandler
permits BalanceIntentionHandler,
                ExchangeRateIntentionHandler,
                ProductInfoIntentionHandler,
                InvestmentRecommendationHandler
{

    /**
     * Determines whether this handler can process the given user message.
     *
     * @param message the user's natural-language query
     * @return {@code true} if this handler recognizes the intention
     */
    boolean canHandle(String message);

    /**
     * Processes the user's message and generates a response.
     *
     * @param message    the user's natural-language query
     * @param customerId the identifier of the requesting customer
     * @return the assistant's response text
     */
    String handle(String message, String customerId);

    /**
     * Returns the canonical name of this intention for tracking and persistence.
     *
     * @return intention identifier (e.g., "consultar_saldo")
     */
    String getIntentionName();
}
