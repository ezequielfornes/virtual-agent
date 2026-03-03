package com.financial.assistant.intention;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Handles balance inquiry intentions.
 *
 * <p>
 * Detects when the user asks about their account balance and returns
 * simulated balance data. In a production environment, this would
 * integrate with the core banking system via a dedicated client.
 * </p>
 *
 * <p>
 * Declared {@code final} as required by the {@link IntentionHandler}
 * sealed interface (Java 21), ensuring a closed set of intention
 * implementations known at compile time.
 * </p>
 */
@Component
@Order(1)
public final class BalanceIntentionHandler implements IntentionHandler {

    private static final Logger log = LoggerFactory.getLogger(BalanceIntentionHandler.class);

    private static final Pattern BALANCE_PATTERN = Pattern.compile(
            ".*(saldo|balance|cuenta|account|disponible|available|fondos|funds).*",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    /** Simulated account balances keyed by customer ID. */
    private static final Map<String, String> SIMULATED_BALANCES = Map.of(
            "123", "15,420.75 USD",
            "456", "8,230.50 EUR",
            "789", "32,100.00 USD");

    private static final String DEFAULT_BALANCE = "24,500.00 USD";

    @Override
    public boolean canHandle(final String message) {
        return BALANCE_PATTERN.matcher(message).matches();
    }

    @Override
    public String handle(final String message, final String customerId) {
        log.info("Processing balance inquiry for customer: {}", customerId);

        final String balance = SIMULATED_BALANCES.getOrDefault(customerId, DEFAULT_BALANCE);
        return ("Your current account balance is %s. "
                + "This information is as of the last business day. "
                + "For real-time balance, please contact your branch or use our mobile app.")
                .formatted(balance);
    }

    @Override
    public String getIntentionName() {
        return "consultar_saldo";
    }
}
