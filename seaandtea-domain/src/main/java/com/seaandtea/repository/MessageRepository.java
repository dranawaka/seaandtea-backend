package com.seaandtea.repository;

import com.seaandtea.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Modifying
    @Query("DELETE FROM Message m WHERE m.sender.id = :userId OR m.receiver.id = :userId")
    void deleteBySenderIdOrReceiverId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Message m WHERE m.booking.id = :bookingId")
    void deleteByBookingId(@Param("bookingId") Long bookingId);

    /** Messages between two users (either direction), newest first. */
    @Query("SELECT m FROM Message m WHERE (m.sender.id = :user1 AND m.receiver.id = :user2) OR (m.sender.id = :user2 AND m.receiver.id = :user1) ORDER BY m.createdAt DESC")
    Page<Message> findConversation(@Param("user1") Long user1, @Param("user2") Long user2, Pageable pageable);

    /** Count of unread messages for a user where they are the receiver. */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :userId AND m.isRead = false")
    long countUnreadByReceiverId(@Param("userId") Long userId);

    /** Count of unread messages from a specific sender to the current user. */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :receiverId AND m.sender.id = :senderId AND m.isRead = false")
    long countUnreadFromSenderToReceiver(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    /** Conversation partners for a user: other user id and last message timestamp. */
    @Query(value = "SELECT CASE WHEN sender_id = :userId THEN receiver_id ELSE sender_id END AS partner_id, MAX(created_at) AS last_at FROM messages WHERE sender_id = :userId OR receiver_id = :userId GROUP BY partner_id ORDER BY last_at DESC", nativeQuery = true)
    List<Object[]> findConversationPartnerIdsAndLastMessageAt(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.sender.id = :senderId AND m.receiver.id = :receiverId AND m.isRead = false")
    int markAsReadBySenderAndReceiver(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
}
