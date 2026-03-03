package com.financial.assistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Main entry point for the Financial Virtual Assistant microservice.
 *
 * <p>
 * This application serves as a conversational orchestrator that processes
 * financial queries using a Strategy-based intention resolution system
 * (sealed interface, Java 21), integrates with external financial APIs,
 * and persists conversation history.
 * </p>
 *
 * <p>
 * Runs on Java 21 with virtual threads enabled for optimal I/O-bound
 * scalability.
 * </p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class FinancialAssistantApplication {

    public static void main(final String[] args) {
        SpringApplication.run(FinancialAssistantApplication.class, args);
    }
}
