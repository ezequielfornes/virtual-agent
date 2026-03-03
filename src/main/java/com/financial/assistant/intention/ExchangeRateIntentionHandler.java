package com.financial.assistant.intention;

import com.financial.assistant.client.ExchangeRateClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles exchange rate inquiry intentions.
 *
 * <p>
 * Detects currency-related queries and delegates to the
 * {@link ExchangeRateClient} for real-time exchange rate data from
 * the Frankfurter API. Supports multiple currency pairs and handles
 * fallback scenarios gracefully.
 * </p>
 *
 * <p>
 * Declared {@code final} as required by the {@link IntentionHandler}
 * sealed interface (Java 21), ensuring a closed set of intention
 * implementations known at compile time.
 * </p>
 */
@Component
@Order(2)
public final class ExchangeRateIntentionHandler implements IntentionHandler {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateIntentionHandler.class);

    private static final Pattern EXCHANGE_RATE_PATTERN = Pattern.compile(
            ".*(tipo de cambio|exchange rate|cambio|divisa|currency|dólar|dollar|euro|libra|pound|yen|"
                    + "cotización|cotizacion|moneda|forex|usd|eur|gbp|jpy|brl).*",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    /** Regex to extract a source currency code from the user message. */
    private static final Pattern CURRENCY_EXTRACTOR = Pattern.compile(
            "\\b(USD|EUR|GBP|JPY|BRL|ARS|MXN|CLP|COP|CHF|CAD|AUD)\\b",
            Pattern.CASE_INSENSITIVE);

    /** Maps common currency names to ISO codes. */
    private static final Map<String, String> CURRENCY_ALIASES = Map.ofEntries(
            Map.entry("dólar", "USD"), Map.entry("dollar", "USD"), Map.entry("dolar", "USD"),
            Map.entry("euro", "EUR"),
            Map.entry("libra", "GBP"), Map.entry("pound", "GBP"),
            Map.entry("yen", "JPY"),
            Map.entry("real", "BRL"), Map.entry("peso", "ARS"));

    private final ExchangeRateClient exchangeRateClient;

    public ExchangeRateIntentionHandler(final ExchangeRateClient exchangeRateClient) {
        this.exchangeRateClient = exchangeRateClient;
    }

    @Override
    public boolean canHandle(final String message) {
        return EXCHANGE_RATE_PATTERN.matcher(message).matches();
    }

    @Override
    public String handle(final String message, final String customerId) {
        final String baseCurrency = extractCurrency(message);
        log.info("Processing exchange rate query for base currency: {}", baseCurrency);

        final Map<String, Double> rates = exchangeRateClient.getLatestRates(baseCurrency);

        if (rates.isEmpty()) {
            return ("I'm sorry, I couldn't retrieve the current exchange rates for %s at this time. "
                    + "Please try again in a few moments.").formatted(baseCurrency);
        }

        final StringBuilder response = new StringBuilder();
        response.append("Current exchange rates for %s:%n".formatted(baseCurrency));

        rates.entrySet().stream()
                .limit(6)
                .forEach(entry -> response.append(
                        "  • 1 %s = %.4f %s%n".formatted(baseCurrency, entry.getValue(), entry.getKey())));

        response.append("Source: Frankfurter API (European Central Bank data).");
        return response.toString();
    }

    @Override
    public String getIntentionName() {
        return "consultar_tipo_cambio";
    }

    /**
     * Extracts the base currency from the message, defaulting to USD.
     */
    private String extractCurrency(final String message) {
        final Matcher codeMatcher = CURRENCY_EXTRACTOR.matcher(message);
        if (codeMatcher.find()) {
            return codeMatcher.group(1).toUpperCase();
        }

        final String lowerMessage = message.toLowerCase();
        for (final Map.Entry<String, String> alias : CURRENCY_ALIASES.entrySet()) {
            if (lowerMessage.contains(alias.getKey())) {
                return alias.getValue();
            }
        }

        return "USD";
    }
}
