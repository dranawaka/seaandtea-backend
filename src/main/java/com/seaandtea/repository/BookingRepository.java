package com.seaandtea.repository;

import com.seaandtea.entity.Booking;
import com.seaandtea.entity.Booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByIdAndTouristId(Long id, Long touristId);

    @Query("SELECT b FROM Booking b WHERE b.id = :id AND b.status = :status")
    Optional<Booking> findByIdAndStatus(@Param("id") Long id, @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.id = :id AND b.tourist.id = :touristId AND b.status = :status")
    Optional<Booking> findByIdAndTouristIdAndStatus(
            @Param("id") Long id,
            @Param("touristId") Long touristId,
            @Param("status") BookingStatus status);
}
