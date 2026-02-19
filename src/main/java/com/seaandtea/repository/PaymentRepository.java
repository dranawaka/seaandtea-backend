package com.seaandtea.repository;

import com.seaandtea.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Modifying
    @Query("DELETE FROM Payment p WHERE p.booking.id = :bookingId")
    void deleteByBookingId(@Param("bookingId") Long bookingId);
}
