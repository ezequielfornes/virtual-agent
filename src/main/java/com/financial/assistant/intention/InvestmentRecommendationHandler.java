package com.financial.assistant.intention;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles investment recommendation intentions.
 *
 * <p>
 * Detects when the user asks for investment advice and generates
 * recommendations based on a simulated risk profile. In production,
 * this would integrate with a risk assessment engine and portfolio
 * management system.
 * </p>
 *
 * <p>
 * Declared {@code final} as required by the {@link IntentionHandler}
 * sealed interface (Java 21), ensuring a closed set of intention
 * implementations known at compile time.
 * </p>
 */
@Component
@Order(4)
public final class InvestmentRecommendationHandler implements IntentionHandler {

        private static final Logger log = LoggerFactory.getLogger(InvestmentRecommendationHandler.class);

        private static final Pattern INVESTMENT_PATTERN = Pattern.compile(
                        ".*(inversión|inversion|invertir|invest|recomendación|recomendacion|recommendation|"
                                        + "portafolio|portfolio|acciones|stocks|bonos|bonds|fondo|fund|rendimiento|yield|"
                                        + "riesgo|risk|rentabilidad|return).*",
                        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

        private static final Pattern RISK_PROFILE_PATTERN = Pattern.compile(
                        "\\b(conservador|conservative|moderado|moderate|agresivo|aggressive|bajo|low|alto|high)\\b",
                        Pattern.CASE_INSENSITIVE);

        /** Investment recommendations by risk profile. */
        private static final Map<String, String> RECOMMENDATIONS = Map.of(
                        "conservative", """
                                        📊 Conservative Investment Portfolio:
                                        Based on your risk profile, we recommend:
                                        • 60% Government Bonds (AAA rated)
                                        • 20% Fixed Term Deposits (4.5% APY)
                                        • 15% Investment Grade Corporate Bonds
                                        • 5% Blue-chip Dividend Stocks

                                        Expected annual return: 4-6%
                                        Risk level: Low | Recommended horizon: 1-3 years""",

                        "moderate", """
                                        📊 Moderate Investment Portfolio:
                                        Based on your risk profile, we recommend:
                                        • 35% Government and Corporate Bonds
                                        • 30% Diversified Equity Index Funds (S&P 500, MSCI World)
                                        • 20% Real Estate Investment Trusts (REITs)
                                        • 10% Emerging Market Funds
                                        • 5% Commodities (Gold ETF)

                                        Expected annual return: 6-10%
                                        Risk level: Medium | Recommended horizon: 3-7 years""",

                        "aggressive", """
                                        📊 Aggressive Growth Portfolio:
                                        Based on your risk profile, we recommend:
                                        • 50% Growth Stocks (Tech, Biotech, Clean Energy)
                                        • 20% Small-Cap and Mid-Cap Funds
                                        • 15% Emerging Market Equities
                                        • 10% Cryptocurrency ETFs
                                        • 5% Venture Capital Funds

                                        Expected annual return: 10-18%
                                        Risk level: High | Recommended horizon: 7+ years""");

        @Override
        public boolean canHandle(final String message) {
                return INVESTMENT_PATTERN.matcher(message).matches();
        }

        @Override
        public String handle(final String message, final String customerId) {
                final String riskProfile = detectRiskProfile(message);
                log.info("Processing investment recommendation for customer: {} with profile: {}",
                                customerId, riskProfile);

                final String recommendation = RECOMMENDATIONS.getOrDefault(riskProfile,
                                RECOMMENDATIONS.get("moderate"));

                return recommendation
                                + "\n\n⚠️ Disclaimer: This is a general recommendation. Past performance does not guarantee "
                                + "future results. Please consult with a certified financial advisor before making "
                                + "investment decisions.";
        }

        @Override
        public String getIntentionName() {
                return "recomendacion_inversion";
        }

        /**
         * Detects the user's risk profile from the message, defaulting to moderate.
         *
         * <p>
         * Uses Java 21 enhanced pattern matching for {@code switch} to map
         * bilingual risk keywords to canonical profile names.
         * </p>
         */
        private String detectRiskProfile(final String message) {
                final Matcher matcher = RISK_PROFILE_PATTERN.matcher(message);
                if (matcher.find()) {
                        return switch (matcher.group(1).toLowerCase()) {
                                case "conservador", "conservative", "bajo", "low" -> "conservative";
                                case "agresivo", "aggressive", "alto", "high" -> "aggressive";
                                default -> "moderate";
                        };
                }
                return "moderate";
        }
}
