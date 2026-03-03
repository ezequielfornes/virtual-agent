package com.financial.assistant.client;

import java.util.Map;

/**
 * DTO representing the JSON response from the Frankfurter exchange rate API.
 *
 * @param base   the base currency code
 * @param date   the date of the rates
 * @param rates  map of currency codes to exchange rates
 */
public record ExchangeRateResponse(
        String base,
        String date,
        Map<String, Double> rates
) {
}
