package com.seaandtea.repository;

import com.seaandtea.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByBookingId(Long bookingId);

    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);

    @Modifying
    @Query("DELETE FROM Payment p WHERE p.booking.id = :bookingId")
    void deleteByBookingId(@Param("bookingId") Long bookingId);
}
