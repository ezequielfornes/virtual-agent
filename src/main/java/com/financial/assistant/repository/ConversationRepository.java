package com.financial.assistant.repository;

import com.financial.assistant.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for {@link Conversation} entities.
 *
 * <p>
 * Provides standard CRUD operations plus custom queries for
 * fetching conversation history by customer.
 * </p>
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * Retrieves all conversations for a given customer, ordered by
     * most recent first.
     *
     * @param customerId the customer's identifier
     * @return list of conversations, most recent first
     */
    List<Conversation> findByCustomerIdOrderByTimestampDesc(String customerId);
}
