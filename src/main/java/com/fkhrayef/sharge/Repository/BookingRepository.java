package com.fkhrayef.sharge.Repository;

import com.fkhrayef.sharge.Model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    Booking findBookingById(Integer id);


    boolean existsByCarIdAndStatusIn(Integer carId, List<String> statuses);

    boolean existsByChargerIdAndStatusIn(Integer chargerId, List<String> statuses);

    List<Booking> findBookingByStatusAndCreatedAtBefore(String status, Instant cutoff);

    @Query("SELECT b FROM Booking b WHERE b.userId = :userId ORDER BY b.createdAt DESC")
    List<Booking> findBookingByUserIdOrderByCreatedAtDesc(Integer userId);

    @Query("SELECT b FROM Booking b WHERE b.carId = :carId AND b.status = :status AND b.createdAt < :cutoff")
    List<Booking> findByCarIdAndStatusAndCreatedAtBefore(Integer carId, String status, Instant cutoff);

    @Query("SELECT b FROM Booking b WHERE b.chargerId = :chargerId AND b.status = :status AND b.createdAt < :cutoff")
    List<Booking> findByChargerIdAndStatusAndCreatedAtBefore(Integer chargerId, String status, Instant cutoff);

    // Charger Statistics Queries
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.chargerId = :chargerId")
    Integer countBookingsByChargerId(Integer chargerId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.chargerId = :chargerId AND b.status = 'COMPLETED'")
    Integer countCompletedBookingsByChargerId(Integer chargerId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.chargerId = :chargerId AND b.status = 'CANCELLED'")
    Integer countCancelledBookingsByChargerId(Integer chargerId);

    // User Statistics Queries
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.userId = :userId AND b.status = 'COMPLETED'")
    Integer countCompletedBookingsByUserId(Integer userId);

    @Query("SELECT b.chargerId, COUNT(b) as bookingCount FROM Booking b WHERE b.userId = :userId GROUP BY b.chargerId ORDER BY bookingCount DESC")
    List<Object[]> findMostUsedChargerByUserId(Integer userId);

    @Query("SELECT b.carId, COUNT(b) as bookingCount FROM Booking b WHERE b.userId = :userId GROUP BY b.carId ORDER BY bookingCount DESC")
    List<Object[]> findMostUsedCarByUserId(Integer userId);

    @Query(value = "SELECT AVG(TIMESTAMPDIFF(MINUTE, start_time, end_time)) FROM booking WHERE user_id = :userId AND status = 'COMPLETED' AND start_time IS NOT NULL AND end_time IS NOT NULL", nativeQuery = true)
    Double avgSessionDurationByUserId(Integer userId);
}
