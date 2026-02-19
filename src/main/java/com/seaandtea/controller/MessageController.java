package com.seaandtea.controller;

import com.seaandtea.dto.ConversationResponse;
import com.seaandtea.dto.MessageCreateRequest;
import com.seaandtea.dto.MessageResponse;
import com.seaandtea.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Messaging", description = "Internal chat between customers, guides and admins")
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Send a message",
        description = "Send a message to another user (customer, guide or admin).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Message sent",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input or cannot send to yourself"),
        @ApiResponse(responseCode = "404", description = "Receiver or booking not found")
    })
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody MessageCreateRequest request,
            Authentication authentication) {
        log.info("Sending message to user {} from {}", request.getReceiverId(), authentication.getName());
        MessageResponse response = messageService.sendMessage(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "List my conversations",
        description = "Get all conversations for the current user (customers, guides, admins).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Conversations retrieved")
    })
    public ResponseEntity<List<ConversationResponse>> getConversations(Authentication authentication) {
        List<ConversationResponse> conversations = messageService.getConversations(authentication.getName());
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/conversations/{partnerId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get messages with a user",
        description = "Get paginated messages between the current user and the given partner.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Messages retrieved"),
        @ApiResponse(responseCode = "400", description = "Invalid partner (e.g. self)"),
        @ApiResponse(responseCode = "404", description = "Partner not found")
    })
    public ResponseEntity<Page<MessageResponse>> getConversationMessages(
            @Parameter(description = "Other user's ID (partner in the conversation)") @PathVariable Long partnerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MessageResponse> messages = messageService.getConversationMessages(
                authentication.getName(), partnerId, pageable);
        return ResponseEntity.ok(messages);
    }

    @PutMapping("/conversations/{partnerId}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Mark conversation as read",
        description = "Mark all messages from the given partner to the current user as read.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Marked as read"),
        @ApiResponse(responseCode = "404", description = "Partner not found")
    })
    public ResponseEntity<Void> markConversationAsRead(
            @Parameter(description = "Partner user ID") @PathVariable Long partnerId,
            Authentication authentication) {
        messageService.markConversationAsRead(authentication.getName(), partnerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get unread message count",
        description = "Get total number of unread messages for the current user.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Unread count")
    })
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        long count = messageService.getUnreadCount(authentication.getName());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }
}
