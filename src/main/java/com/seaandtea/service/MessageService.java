package com.seaandtea.service;

import com.seaandtea.dto.ConversationResponse;
import com.seaandtea.dto.MessageCreateRequest;
import com.seaandtea.dto.MessageResponse;
import com.seaandtea.entity.Booking;
import com.seaandtea.entity.Message;
import com.seaandtea.entity.User;
import com.seaandtea.exception.ResourceNotFoundException;
import com.seaandtea.repository.BookingRepository;
import com.seaandtea.repository.MessageRepository;
import com.seaandtea.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public MessageResponse sendMessage(MessageCreateRequest request, String senderEmail) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", senderEmail));
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getReceiverId()));

        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Cannot send a message to yourself");
        }

        Booking booking = null;
        if (request.getBookingId() != null) {
            booking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Booking", request.getBookingId()));
        }

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .booking(booking)
                .message(request.getMessage().trim())
                .isRead(false)
                .build();
        message = messageRepository.save(message);

        log.info("Message sent from user {} to user {}", sender.getId(), receiver.getId());
        return toMessageResponse(message);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversations(String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        Long userId = currentUser.getId();

        List<Object[]> rows = messageRepository.findConversationPartnerIdsAndLastMessageAt(userId);
        List<ConversationResponse> result = new ArrayList<>();

        for (Object[] row : rows) {
            Long partnerId = ((Number) row[0]).longValue();
            LocalDateTime lastAt = row[1] instanceof Timestamp
                    ? ((Timestamp) row[1]).toLocalDateTime()
                    : (LocalDateTime) row[1];

            User partner = userRepository.findById(partnerId)
                    .orElse(null);
            if (partner == null) continue;

            long unreadCount = messageRepository.countUnreadFromSenderToReceiver(partnerId, userId);
            String lastMessagePreview = getLastMessagePreview(userId, partnerId);

            result.add(ConversationResponse.builder()
                    .partnerId(partner.getId())
                    .partnerName(partner.getFirstName() + " " + partner.getLastName())
                    .partnerEmail(partner.getEmail())
                    .partnerRole(partner.getRole().name())
                    .lastMessagePreview(lastMessagePreview)
                    .lastMessageAt(lastAt)
                    .unreadCount(unreadCount)
                    .build());
        }

        return result;
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> getConversationMessages(String userEmail, Long otherUserId, Pageable pageable) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        userRepository.findById(otherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", otherUserId));

        Long userId = currentUser.getId();
        if (userId.equals(otherUserId)) {
            throw new IllegalArgumentException("Cannot view conversation with yourself");
        }

        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Message> messages = messageRepository.findConversation(userId, otherUserId, sorted);
        return messages.map(this::toMessageResponse);
    }

    @Transactional
    public void markConversationAsRead(String userEmail, Long partnerId) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        messageRepository.markAsReadBySenderAndReceiver(partnerId, currentUser.getId());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        return messageRepository.countUnreadByReceiverId(currentUser.getId());
    }

    private String getLastMessagePreview(Long userId, Long partnerId) {
        Page<Message> one = messageRepository.findConversation(userId, partnerId, PageRequest.of(0, 1));
        if (one.isEmpty()) return "";
        String text = one.getContent().get(0).getMessage();
        return text.length() > 100 ? text.substring(0, 97) + "..." : text;
    }

    private MessageResponse toMessageResponse(Message m) {
        return MessageResponse.builder()
                .id(m.getId())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getFirstName() + " " + m.getSender().getLastName())
                .receiverId(m.getReceiver().getId())
                .receiverName(m.getReceiver().getFirstName() + " " + m.getReceiver().getLastName())
                .bookingId(m.getBooking() != null ? m.getBooking().getId() : null)
                .message(m.getMessage())
                .isRead(Boolean.TRUE.equals(m.getIsRead()))
                .createdAt(m.getCreatedAt())
                .build();
    }
}
