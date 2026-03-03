package com.financial.assistant.intention;

import com.financial.assistant.client.ExchangeRateClient;
import com.financial.assistant.exception.UnrecognizedIntentionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link IntentionResolver}.
 *
 * <p>
 * Verifies correct handler resolution, priority ordering,
 * and exception handling when no handler matches.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class IntentionResolverTest {

    @Mock
    private ExchangeRateClient exchangeRateClient;

    @Test
    @DisplayName("Should resolve to BalanceIntentionHandler for balance-related messages")
    void shouldResolveBalanceIntention() {
        final IntentionResolver resolver = new IntentionResolver(List.of(
                new BalanceIntentionHandler(),
                new ExchangeRateIntentionHandler(exchangeRateClient),
                new ProductInfoIntentionHandler(),
                new InvestmentRecommendationHandler()));

        final IntentionHandler result = resolver.resolve("¿Cuál es mi saldo?");

        assertInstanceOf(BalanceIntentionHandler.class, result);
        assertEquals("consultar_saldo", result.getIntentionName());
    }

    @Test
    @DisplayName("Should resolve to ExchangeRateIntentionHandler for currency-related messages")
    void shouldResolveExchangeRateIntention() {
        final IntentionResolver resolver = new IntentionResolver(List.of(
                new BalanceIntentionHandler(),
                new ExchangeRateIntentionHandler(exchangeRateClient),
                new ProductInfoIntentionHandler(),
                new InvestmentRecommendationHandler()));

        final IntentionHandler result = resolver.resolve("tipo de cambio del euro");

        assertInstanceOf(ExchangeRateIntentionHandler.class, result);
    }

    @Test
    @DisplayName("Should throw UnrecognizedIntentionException when no handler matches")
    void shouldThrowWhenNoHandlerMatches() {
        final IntentionResolver resolver = new IntentionResolver(List.of(
                new BalanceIntentionHandler(),
                new ExchangeRateIntentionHandler(exchangeRateClient),
                new ProductInfoIntentionHandler(),
                new InvestmentRecommendationHandler()));

        assertThrows(UnrecognizedIntentionException.class,
                () -> resolver.resolve("tell me a joke"));
    }

    @Test
    @DisplayName("Should respect handler order — first matching handler wins")
    void shouldRespectHandlerOrder() {
        // "inversión" could also match "info" via "info" keyword overlap, but
        // Investment handler is listed first, so it should win.
        final IntentionResolver resolver = new IntentionResolver(List.of(
                new InvestmentRecommendationHandler(),
                new ProductInfoIntentionHandler()));

        final IntentionHandler result = resolver.resolve("recomendación de inversión");

        assertInstanceOf(InvestmentRecommendationHandler.class, result);
    }
}
