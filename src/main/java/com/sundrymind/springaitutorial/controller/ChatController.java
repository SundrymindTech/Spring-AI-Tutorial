// src/main/java/com/example/springaitutorial/controller/ChatController.java
package com.sundrymind.springaitutorial.controller;

import com.sundrymind.springaitutorial.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*") // Allow requests from any origin (for development)
public class ChatController {

	private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

	private final ChatService chatService;

	/**
	 * Constructor injection of ChatService
	 * Spring automatically provides the ChatService instance
	 */
	public ChatController(ChatService chatService) {
		this.chatService = chatService;
	}

	/**
	 * Endpoint for single message interactions (stateless)
	 * POST /api/chat/message
	 *
	 * @param request Contains the user message
	 * @return ChatResponse with AI reply and new conversation ID
	 */
	@PostMapping("/message")
	public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
		logger.info("Received message request");

		// Validate input
		if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
			logger.warn("Empty message received");
			return ResponseEntity.badRequest()
					.body(new ChatResponse("Message cannot be empty", null));
		}

		// Validate message length (prevent abuse)
		if (request.getMessage().length() > 1000) {
			logger.warn("Message too long: {} characters", request.getMessage().length());
			return ResponseEntity.badRequest()
					.body(new ChatResponse("Message too long. Please limit to 1000 characters.", null));
		}

		try {
			// Generate AI response
			String response = chatService.generateResponse(request.getMessage());

			// Generate new conversation ID for potential follow-up
			String conversationId = UUID.randomUUID().toString();

			logger.info("Message processed successfully");
			return ResponseEntity.ok(new ChatResponse(response, conversationId));

		} catch (Exception e) {
			logger.error("Unexpected error processing message", e);
			return ResponseEntity.internalServerError()
					.body(new ChatResponse("Service temporarily unavailable. Please try again later.", null));
		}
	}

	/**
	 * Endpoint for continuing an existing conversation (stateful)
	 * POST /api/chat/conversation/{conversationId}
	 *
	 * @param conversationId ID of existing conversation
	 * @param request Contains the user message
	 * @return ChatResponse with contextual AI reply
	 */
	@PostMapping("/conversation/{conversationId}")
	public ResponseEntity<ChatResponse> continueConversation(
			@PathVariable String conversationId,
			@RequestBody ChatRequest request) {

		logger.info("Continuing conversation: {}", conversationId);

		// Validate conversation ID
		if (conversationId == null || conversationId.trim().isEmpty()) {
			return ResponseEntity.badRequest()
					.body(new ChatResponse("Invalid conversation ID", null));
		}

		// Validate message
		if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
			return ResponseEntity.badRequest()
					.body(new ChatResponse("Message cannot be empty", conversationId));
		}

		try {
			// Generate contextual response
			String response = chatService.generateResponseWithContext(
					request.getMessage(),
					conversationId
					);

			return ResponseEntity.ok(new ChatResponse(response, conversationId));

		} catch (Exception e) {
			logger.error("Error continuing conversation: {}", conversationId, e);
			return ResponseEntity.internalServerError()
					.body(new ChatResponse("Unable to continue conversation. Please try again.", conversationId));
		}
	}

	/**
	 * Health check endpoint
	 * GET /api/chat/health
	 *
	 * @return Service status information
	 */
	@GetMapping("/health")
	public ResponseEntity<Map<String, Object>> healthCheck() {
		boolean isHealthy = chatService.isServiceHealthy();

		Map<String, Object> status = Map.of(
				"status", isHealthy ? "healthy" : "unhealthy",
						"service", "AI Chat Service",
						"timestamp", System.currentTimeMillis()
				);

		// Return appropriate HTTP status
		return isHealthy ?
				ResponseEntity.ok(status) :
					ResponseEntity.status(503).body(status);
	}

	/**
	 * Get conversation statistics (bonus endpoint)
	 * GET /api/chat/stats
	 */
	@GetMapping("/stats")
	public ResponseEntity<Map<String, Object>> getStats() {
		// In a real application, you'd track these metrics
		Map<String, Object> stats = Map.of(
				"totalConversations", 0,
				"totalMessages", 0,
				"averageResponseTime", "0ms",
				"uptime", System.currentTimeMillis()
				);

		return ResponseEntity.ok(stats);
	}

	// DTO Classes for Request/Response

	/**
	 * Request object for chat messages
	 */
	public static class ChatRequest {
		private String message;

		// Default constructor for JSON deserialization
		public ChatRequest() {}

		public ChatRequest(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		@Override
		public String toString() {
			return "ChatRequest{message='" +
					(message != null ? message.substring(0, Math.min(50, message.length())) : "null") +
					"'}";
		}
	}

	/**
	 * Response object for chat messages
	 */
	public static class ChatResponse {
		private String response;
		private String conversationId;
		private long timestamp;

		public ChatResponse(String response, String conversationId) {
			this.response = response;
			this.conversationId = conversationId;
			this.timestamp = System.currentTimeMillis();
		}

		// Getters
		public String getResponse() { return response; }
		public String getConversationId() { return conversationId; }
		public long getTimestamp() { return timestamp; }

		@Override
		public String toString() {
			return "ChatResponse{conversationId='" + conversationId +
					"', timestamp=" + timestamp +
					", responseLength=" + (response != null ? response.length() : 0) + "}";
		}
	}
}