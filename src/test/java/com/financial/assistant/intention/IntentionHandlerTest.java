package com.financial.assistant.intention;

import com.financial.assistant.client.ExchangeRateClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for individual {@link IntentionHandler} implementations.
 *
 * <p>
 * Each nested class tests a specific handler's keyword detection
 * and response generation logic.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class IntentionHandlerTest {

    @Nested
    @DisplayName("BalanceIntentionHandler")
    class BalanceHandlerTests {

        private final BalanceIntentionHandler handler = new BalanceIntentionHandler();

        @Test
        @DisplayName("Should detect balance keyword 'saldo'")
        void shouldDetectSaldoKeyword() {
            assertTrue(handler.canHandle("¿Cuál es mi saldo?"));
        }

        @Test
        @DisplayName("Should detect balance keyword 'balance'")
        void shouldDetectBalanceKeyword() {
            assertTrue(handler.canHandle("What is my balance?"));
        }

        @Test
        @DisplayName("Should not detect unrelated messages")
        void shouldNotDetectUnrelatedMessage() {
            assertFalse(handler.canHandle("Hello, good morning"));
        }

        @Test
        @DisplayName("Should return simulated balance for known customer")
        void shouldReturnBalanceForKnownCustomer() {
            final String response = handler.handle("show me my balance", "123");
            assertTrue(response.contains("15,420.75 USD"));
        }

        @Test
        @DisplayName("Should return default balance for unknown customer")
        void shouldReturnDefaultBalanceForUnknownCustomer() {
            final String response = handler.handle("show me my balance", "unknown");
            assertTrue(response.contains("24,500.00 USD"));
        }

        @Test
        @DisplayName("Should return 'consultar_saldo' as intention name")
        void shouldReturnCorrectIntentionName() {
            assertEquals("consultar_saldo", handler.getIntentionName());
        }
    }

    @Nested
    @DisplayName("ExchangeRateIntentionHandler")
    class ExchangeRateHandlerTests {

        @Mock
        private ExchangeRateClient exchangeRateClient;

        @InjectMocks
        private ExchangeRateIntentionHandler handler;

        @Test
        @DisplayName("Should detect exchange rate keyword 'tipo de cambio'")
        void shouldDetectTipoDeCambioKeyword() {
            assertTrue(handler.canHandle("¿Cuál es el tipo de cambio del euro?"));
        }

        @Test
        @DisplayName("Should detect currency code 'USD'")
        void shouldDetectUsdKeyword() {
            assertTrue(handler.canHandle("Exchange rate for USD"));
        }

        @Test
        @DisplayName("Should return rates when API responds successfully")
        void shouldReturnRatesFromApi() {
            when(exchangeRateClient.getLatestRates("EUR"))
                    .thenReturn(Map.of("USD", 1.0856, "GBP", 0.8612));

            final String response = handler.handle("tipo de cambio del euro", "123");

            assertTrue(response.contains("EUR"));
            assertTrue(response.contains("Frankfurter"));
        }

        @Test
        @DisplayName("Should return error message when API returns empty rates")
        void shouldReturnErrorMessageWhenApiReturnsEmpty() {
            when(exchangeRateClient.getLatestRates(anyString()))
                    .thenReturn(Collections.emptyMap());

            final String response = handler.handle("exchange rate USD", "123");

            assertTrue(response.contains("couldn't retrieve"));
        }

        @Test
        @DisplayName("Should return 'consultar_tipo_cambio' as intention name")
        void shouldReturnCorrectIntentionName() {
            assertEquals("consultar_tipo_cambio", handler.getIntentionName());
        }
    }

    @Nested
    @DisplayName("ProductInfoIntentionHandler")
    class ProductInfoHandlerTests {

        private final ProductInfoIntentionHandler handler = new ProductInfoIntentionHandler();

        @Test
        @DisplayName("Should detect product keyword 'tarjeta'")
        void shouldDetectTarjetaKeyword() {
            assertTrue(handler.canHandle("Quiero info sobre tarjeta de crédito"));
        }

        @Test
        @DisplayName("Should detect product keyword 'loan'")
        void shouldDetectLoanKeyword() {
            assertTrue(handler.canHandle("Tell me about personal loan options"));
        }

        @Test
        @DisplayName("Should return specific product info when keyword matches catalog")
        void shouldReturnSpecificProductInfo() {
            final String response = handler.handle(
                    "Información sobre tarjeta de crédito", "123");
            assertTrue(response.contains("Premium Credit Card"));
        }

        @Test
        @DisplayName("Should return generic catalog when no specific product matches")
        void shouldReturnGenericCatalogForGeneralQuery() {
            final String response = handler.handle("Tell me about your products", "123");
            assertTrue(response.contains("wide range"));
        }

        @Test
        @DisplayName("Should return 'info_producto' as intention name")
        void shouldReturnCorrectIntentionName() {
            assertEquals("info_producto", handler.getIntentionName());
        }
    }

    @Nested
    @DisplayName("InvestmentRecommendationHandler")
    class InvestmentRecommendationHandlerTests {

        private final InvestmentRecommendationHandler handler = new InvestmentRecommendationHandler();

        @Test
        @DisplayName("Should detect investment keyword 'inversión'")
        void shouldDetectInversionKeyword() {
            assertTrue(handler.canHandle("Quiero una recomendación de inversión"));
        }

        @Test
        @DisplayName("Should detect risk profile 'conservative'")
        void shouldDetectConservativeProfile() {
            final String response = handler.handle(
                    "I want conservative investment advice", "123");
            assertTrue(response.contains("Conservative"));
            assertTrue(response.contains("Government Bonds"));
        }

        @Test
        @DisplayName("Should detect risk profile 'aggressive'")
        void shouldDetectAggressiveProfile() {
            final String response = handler.handle(
                    "I want aggressive investment options", "123");
            assertTrue(response.contains("Aggressive"));
            assertTrue(response.contains("Growth Stocks"));
        }

        @Test
        @DisplayName("Should default to moderate when no profile specified")
        void shouldDefaultToModerateProfile() {
            final String response = handler.handle(
                    "Give me investment recommendations", "123");
            assertTrue(response.contains("Moderate"));
        }

        @Test
        @DisplayName("Should include disclaimer in response")
        void shouldIncludeDisclaimer() {
            final String response = handler.handle("investment advice", "123");
            assertTrue(response.contains("Disclaimer"));
        }

        @Test
        @DisplayName("Should return 'recomendacion_inversion' as intention name")
        void shouldReturnCorrectIntentionName() {
            assertEquals("recomendacion_inversion", handler.getIntentionName());
        }
    }
}
