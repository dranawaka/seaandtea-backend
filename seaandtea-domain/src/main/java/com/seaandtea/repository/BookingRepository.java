package com.seaandtea.repository;

import com.seaandtea.entity.Booking;
import com.seaandtea.entity.Booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByTouristId(Long touristId);

    List<Booking> findByTouristIdOrderByCreatedAtDesc(Long touristId);

    @Query("SELECT COALESCE(SUM(b.numberOfPeople), 0) FROM Booking b WHERE b.tour.id = :tourId AND b.bookingDate = :date AND b.status IN :statuses")
    Long sumPeopleOnDate(
            @Param("tourId") Long tourId,
            @Param("date") LocalDate date,
            @Param("statuses") Collection<BookingStatus> statuses);

    @Query("SELECT b.bookingDate, COALESCE(SUM(b.numberOfPeople), 0) FROM Booking b WHERE b.tour.id = :tourId AND b.bookingDate BETWEEN :from AND :to AND b.status IN :statuses GROUP BY b.bookingDate")
    List<Object[]> sumPeopleBetween(
            @Param("tourId") Long tourId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("statuses") Collection<BookingStatus> statuses);

    List<Booking> findByGuideId(Long guideId);

    Optional<Booking> findByIdAndTouristId(Long id, Long touristId);

    @Query("SELECT b FROM Booking b WHERE b.id = :id AND b.status = :status")
    Optional<Booking> findByIdAndStatus(@Param("id") Long id, @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.id = :id AND b.tourist.id = :touristId AND b.status = :status")
    Optional<Booking> findByIdAndTouristIdAndStatus(
            @Param("id") Long id,
            @Param("touristId") Long touristId,
            @Param("status") BookingStatus status);
}
