package com.seaandtea.repository;

import com.seaandtea.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Modifying
    @Query("DELETE FROM Message m WHERE m.sender.id = :userId OR m.receiver.id = :userId")
    void deleteBySenderIdOrReceiverId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Message m WHERE m.booking.id = :bookingId")
    void deleteByBookingId(@Param("bookingId") Long bookingId);
}
