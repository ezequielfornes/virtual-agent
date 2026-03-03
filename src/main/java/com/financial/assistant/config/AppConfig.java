package com.financial.assistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Application-wide configuration for HTTP clients and shared beans.
 *
 * <p>
 * Configures a {@link RestClient} with externalized timeout settings for
 * communicating with financial data APIs.
 * </p>
 *
 * <p>
 * With Java 21 virtual threads enabled
 * ({@code spring.threads.virtual.enabled=true}),
 * the underlying I/O operations of this {@link RestClient} are automatically
 * executed on virtual threads, providing massive scalability for concurrent
 * external API calls without thread pool exhaustion.
 * </p>
 */
@Configuration
public class AppConfig {

    /**
     * Creates a pre-configured {@link RestClient} for external API calls.
     *
     * <p>
     * When virtual threads are enabled, each HTTP call made through this
     * client runs on a lightweight virtual thread, allowing thousands of
     * concurrent requests with minimal memory overhead.
     * </p>
     *
     * @param properties externalized API configuration
     * @return configured RestClient instance
     */
    @Bean
    public RestClient restClient(final ExternalApiProperties properties) {
        final SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.connectTimeoutMs());
        factory.setReadTimeout(properties.readTimeoutMs());

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(factory)
                .build();
    }
}
