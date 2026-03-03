package com.financial.assistant.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * JPA entity representing a single conversation exchange between
 * a customer and the financial assistant.
 *
 * <p>Stores the full context of each interaction: the customer's message,
 * the detected intention, and the assistant's response. Designed for
 * extensibility — additional fields such as {@code rating} or {@code feedback}
 * can be added without breaking existing functionality.</p>
 */
@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "user_message", nullable = false, length = 2000)
    private String userMessage;

    @Column(name = "assistant_response", nullable = false, length = 4000)
    private String assistantResponse;

    @Column(name = "intention", nullable = false)
    private String intention;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /** Default constructor required by JPA. */
    protected Conversation() {
    }

    /**
     * Creates a new conversation record.
     *
     * @param customerId        identifier of the customer
     * @param userMessage       the customer's original message
     * @param assistantResponse the assistant's generated response
     * @param intention         the detected financial intention
     * @param timestamp         when the conversation occurred
     */
    public Conversation(final String customerId,
                        final String userMessage,
                        final String assistantResponse,
                        final String intention,
                        final LocalDateTime timestamp) {
        this.customerId = customerId;
        this.userMessage = userMessage;
        this.assistantResponse = assistantResponse;
        this.intention = intention;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getAssistantResponse() {
        return assistantResponse;
    }

    public String getIntention() {
        return intention;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
