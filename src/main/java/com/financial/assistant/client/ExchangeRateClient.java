package com.financial.assistant.client;

import java.util.Map;

/**
 * Abstraction for exchange rate data providers.
 *
 * <p>
 * Decouples the application from any specific exchange rate API.
 * To switch providers (e.g., from Frankfurter to XE or Oanda),
 * implement this interface in a new class and update the Spring
 * configuration — no changes to business logic required.
 * </p>
 */
public interface ExchangeRateClient {

    /**
     * Retrieves the latest exchange rates for a given base currency.
     *
     * @param baseCurrency ISO 4217 currency code (e.g., "USD", "EUR")
     * @return map of target currency codes to exchange rates, or empty map on
     *         failure
     */
    Map<String, Double> getLatestRates(String baseCurrency);
}
