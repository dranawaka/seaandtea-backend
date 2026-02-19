package com.seaandtea.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {

    /** The other participant in the conversation (not the current user). */
    private Long partnerId;
    private String partnerName;
    private String partnerEmail;
    /** Role of the partner: USER, GUIDE, ADMIN. */
    private String partnerRole;
    /** Last message preview (snippet). */
    private String lastMessagePreview;
    private LocalDateTime lastMessageAt;
    /** Number of unread messages from partner to current user. */
    private long unreadCount;
}
