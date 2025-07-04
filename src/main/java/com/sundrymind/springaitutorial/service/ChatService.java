// src/main/java/com/example/springaitutorial/service/ChatService.java
package com.sundrymind.springaitutorial.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    // The main interface for AI interactions
    private final ChatClient chatClient;

    /**
     * Constructor that configures the ChatClient with default settings
     *
     * @param chatClientBuilder Auto-injected builder from Spring AI
     */
    public ChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
            // Set default system prompt that defines AI behavior
            .defaultSystem("You are a friendly AI assistant. " +
                         "Keep responses concise and helpful. " +
                         "Be conversational but professional.")
            // Add memory advisor for conversation context
            .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
            .build();

        logger.info("ChatService initialized with Ollama client");
    }

    /**
     * Generates a simple response without conversation context
     * Used for stateless interactions
     *
     * @param userMessage The user's input message
     * @return AI-generated response or error message
     */
    public String generateResponse(String userMessage) {
        // Log the request (truncated for privacy)
        logger.debug("Processing message: {}",
                    userMessage.substring(0, Math.min(50, userMessage.length())));

        try {
            // Create a prompt and get response
            String response = chatClient
                .prompt()                    // Start building a prompt
                .user(userMessage)          // Add user message
                .call()                     // Make the API call
                .content();                 // Extract response content

            logger.debug("Generated response successfully");
            return response;

        } catch (Exception e) {
            // Log error and return user-friendly message
            logger.error("Error generating AI response: {}", e.getMessage(), e);
            return "I'm sorry, I'm having trouble processing your request right now. " +
                   "Please try again in a moment.";
        }
    }

    /**
     * Generates response with conversation context
     * Maintains conversation history for more coherent interactions
     *
     * @param userMessage The user's input message
     * @param conversationId Unique identifier for this conversation
     * @return AI-generated response with conversation context
     */
    public String generateResponseWithContext(String userMessage, String conversationId) {
        logger.debug("Processing contextual message for conversation: {}", conversationId);

        try {
            return chatClient
                .prompt()
                .user(userMessage)
                // Configure memory advisor with conversation ID
                .advisors(advisorSpec -> advisorSpec
                    .param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY,
                           conversationId))
                .call()
                .content();

        } catch (Exception e) {
            logger.error("Error generating contextual response: {}", e.getMessage(), e);
            return "I apologize, but I'm experiencing technical difficulties. " +
                   "Please try again in a moment.";
        }
    }

    /**
     * Health check method to verify AI service is working
     * Used by monitoring endpoints
     *
     * @return true if service is healthy, false otherwise
     */
    public boolean isServiceHealthy() {
        try {
            // Send a simple test message
            String testResponse = chatClient
                .prompt()
                .user("Hello")
                .call()
                .content();

            // Check if we got a valid response
            boolean isHealthy = testResponse != null && !testResponse.trim().isEmpty();
            logger.debug("Health check result: {}", isHealthy);
            return isHealthy;

        } catch (Exception e) {
            logger.warn("Health check failed: {}", e.getMessage());
            return false;
        }
    }
}