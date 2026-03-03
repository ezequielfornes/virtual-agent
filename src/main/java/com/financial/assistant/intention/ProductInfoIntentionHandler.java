package com.financial.assistant.intention;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Handles product information inquiry intentions.
 *
 * <p>
 * Detects when the user asks about financial products and returns
 * predefined information. In a production system, this would query
 * a product catalog service.
 * </p>
 *
 * <p>
 * Declared {@code final} as required by the {@link IntentionHandler}
 * sealed interface (Java 21), ensuring a closed set of intention
 * implementations known at compile time.
 * </p>
 */
@Component
@Order(3)
public final class ProductInfoIntentionHandler implements IntentionHandler {

        private static final Logger log = LoggerFactory.getLogger(ProductInfoIntentionHandler.class);

        private static final Pattern PRODUCT_PATTERN = Pattern.compile(
                        ".*(producto|product|tarjeta|card|préstamo|prestamo|loan|hipoteca|mortgage|"
                                        + "seguro|insurance|depósito|deposito|deposit|plazo fijo|crédito|credito|credit|"
                                        + "cuenta corriente|checking|savings|ahorro|info).*",
                        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

        /** Catalog of financial products with descriptions. */
        private static final Map<String, String> PRODUCT_CATALOG = Map.of(
                        "tarjeta", """
                                        💳 Premium Credit Card:
                                        • Annual fee: $99 (waived first year)
                                        • Cashback: 2% on all purchases, 5% on travel
                                        • Travel insurance included
                                        • Airport lounge access worldwide
                                        • Contactless and mobile payments enabled""",
                        "préstamo", """
                                        🏦 Personal Loan:
                                        • Rates from 8.5% APR
                                        • Amounts: $1,000 - $50,000
                                        • Terms: 12 to 60 months
                                        • No early repayment fees
                                        • Pre-approval in 24 hours""",
                        "hipoteca", """
                                        🏠 Mortgage:
                                        • Fixed rate from 4.2% APR
                                        • Up to 80% financing
                                        • Terms: 10 to 30 years
                                        • Free property appraisal
                                        • Dedicated mortgage advisor""",
                        "deposito", """
                                        💰 Fixed Term Deposit:
                                        • Rates up to 4.8% APY
                                        • Terms: 30 days to 5 years
                                        • Minimum deposit: $500
                                        • Interest paid monthly or at maturity
                                        • FDIC insured""");

        @Override
        public boolean canHandle(final String message) {
                return PRODUCT_PATTERN.matcher(message).matches();
        }

        @Override
        public String handle(final String message, final String customerId) {
                log.info("Processing product info request for customer: {}", customerId);

                final String lowerMessage = message.toLowerCase();

                for (final Map.Entry<String, String> entry : PRODUCT_CATALOG.entrySet()) {
                        if (lowerMessage.contains(entry.getKey())) {
                                return entry.getValue()
                                                + "\n\nWould you like to schedule an appointment with a specialist or get more details?";
                        }
                }

                return """
                                We offer a wide range of financial products:
                                • 💳 Credit and Debit Cards
                                • 🏦 Personal and Business Loans
                                • 🏠 Mortgages
                                • 💰 Fixed Term Deposits and Savings Accounts
                                • 🛡️ Insurance Products

                                Please specify which product you'd like to learn more about.""";
        }

        @Override
        public String getIntentionName() {
                return "info_producto";
        }
}
