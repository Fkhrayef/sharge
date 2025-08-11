package com.fkhrayef.sharge.Service;

import com.fkhrayef.sharge.Api.ApiException;
import com.fkhrayef.sharge.Model.*;
import com.fkhrayef.sharge.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final int HOLD_WINDOW_SECONDS = 1200;

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final ChargerRepository chargerRepository;
    private final ChargerAvailabilityRepository chargerAvailabilityRepository;
    private final ChargerConnectorRepository chargerConnectorRepository;
    private final InvoiceRepository invoiceRepository;
    private final HereService hereService;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getUserBookings(Integer userId) {
        return bookingRepository.findBookingByUserIdOrderByCreatedAtDesc(userId);
    }

    public void reserveCharger(Integer userId, Integer carId, Integer chargerId, Double userLat, Double userLng) {
        cleanupExpiredForCar(carId);
        cleanupExpiredForCharger(chargerId);

        // Validate IDs
        if (userRepository.findUserById(userId) == null) {
            throw new ApiException("User not found");
        }
        if (carRepository.findCarById(carId) == null) {
            throw new ApiException("Car not found");
        }
        if (!carRepository.findCarById(carId).getUserId().equals(userId)) {
            throw new ApiException("Car does not belong to the user");
        }
        if (bookingRepository.existsByCarIdAndStatusIn(carId, List.of("RESERVED", "IN_PROGRESS"))) {
            throw new ApiException("Car has another active booking");
        }
        if (chargerRepository.findChargerById(chargerId) == null) {
            throw new ApiException("Charger not found");
        }
        if (!chargerRepository.findChargerById(chargerId).getIsActive()) {
            throw new ApiException("Charger is inactive at the moment");
        }
        if (bookingRepository.existsByChargerIdAndStatusIn(chargerId, List.of("RESERVED", "IN_PROGRESS"))) {
            throw new ApiException("Charger has another active booking");
        }
        if (!chargerConnectorRepository.existsByChargerIdAndConnectorType(chargerId, carRepository.findCarById(carId).getConnectorType())) {
            throw new ApiException("Charger does not have a connector of the same type as the car");
        }
        if (!isWithinAvailabilityWindow(chargerId, LocalDateTime.now())) {
            throw new ApiException("Charger is not available at the moment");
        }

        // Convert chargers to destinations
        List<Charger> bookedCharger = new ArrayList<>();
        bookedCharger.add(chargerRepository.findChargerById(chargerId));

        List<Map<String, Object>> destination = bookedCharger.stream()
                .map(charger -> Map.of(
                        "lat", charger.getLatitude(),
                        "lng", charger.getLongitude(),
                        "name", charger.getName(),
                        "charger", charger // Include full charger object
                ))
                .toList();

        // get trip in seconds
        List<Map<String, Object>> chargerJourney = hereService.calculateTravelTimes(userLat, userLng, destination);
        int durationSeconds = (Integer) chargerJourney.get(0).get("travelTimeSeconds");

        // get attributes to getChargerAvailabilitiesByCurrentTime
        LocalDateTime now = LocalDateTime.now();
        String currentDay = now.getDayOfWeek().toString(); // "MONDAY", "TUESDAY", etc.
        LocalTime currentTime = now.toLocalTime();

        // get the available time now
        ChargerAvailability chargerAvailability = chargerAvailabilityRepository.getChargerAvailabilitiesByCurrentTime(chargerId, currentDay, currentTime);

        LocalTime arrivalTime = currentTime.plusSeconds(durationSeconds);
        LocalTime minimumChargingEndTime = arrivalTime.plusSeconds(1800); // 30 minutes after arrival

        // Warn if charger closes before user gets 30min of charging
        if (chargerAvailability.getEndTime().isBefore(minimumChargingEndTime)) {
            Duration chargingTimeAvailable = Duration.between(arrivalTime, chargerAvailability.getEndTime());
            System.out.println("WARNING: Only " + chargingTimeAvailable.toMinutes() + " minutes of charging time available!");
        }

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setCarId(carId);
        booking.setChargerId(chargerId);
        booking.setStatus("RESERVED");
        bookingRepository.save(booking);
    }

    public void confirmArrival(Integer userId, Integer bookingId) {

        Booking booking = bookingRepository.findBookingById(bookingId);
        if (booking == null) {
            throw new ApiException("Booking not found");
        }
        if (!booking.getStatus().equals("RESERVED")) {
            throw new ApiException("Booking is not RESERVED");
        }
        if (!booking.getUserId().equals(userId)) {
            throw new ApiException("Booking does not belong to the user");
        }
        if (booking.getCreatedAt().isBefore(Instant.now().minusSeconds(HOLD_WINDOW_SECONDS))) {
            throw new ApiException("Booking has expired");
        }
        if (!isWithinAvailabilityWindow(booking.getChargerId(), LocalDateTime.now())) {
            throw new ApiException("Charger is not available at the moment");
        }

        booking.setStatus("IN_PROGRESS");
        booking.setStartTime(LocalDateTime.now());
        bookingRepository.save(booking);
    }

    public void endSession(Integer userId, Integer bookingId) {

        Booking booking = bookingRepository.findBookingById(bookingId);
        if (booking == null) {
            throw new ApiException("Booking not found");
        }
        if (!booking.getStatus().equals("IN_PROGRESS")) {
            throw new ApiException("Booking is not IN_PROGRESS");
        }
        if (!booking.getUserId().equals(userId)) {
            throw new ApiException("Booking does not belong to the user");
        }

        booking.setStatus("COMPLETED");
        booking.setEndTime(LocalDateTime.now());
        bookingRepository.save(booking);

        Invoice invoice = new Invoice();
        invoice.setBookingId(bookingId);
        invoice.setPaymentMethod("CASH"); // Default
        invoice.setIsPaid(false);

        // session duration
        Duration duration = Duration.between(booking.getStartTime(), booking.getEndTime());
        double hours = duration.toMinutes() / 60.0;

        // Calculate the cost
        Car car = carRepository.findCarById(booking.getCarId());
        Charger charger = chargerRepository.findChargerById(booking.getChargerId());
        double estimatedKwh = Math.min(car.getMaxKw(), charger.getMaxKw()) * hours;
        double totalCost = estimatedKwh * charger.getPricePerKwh();


        invoice.setKwhDelivered(estimatedKwh);
        invoice.setTotalCost(totalCost);
        invoiceRepository.save(invoice);
    }

    public void cancelReservation(Integer userId, Integer bookingId) {
        Booking booking = bookingRepository.findBookingById(bookingId);
        if (booking == null) {
            throw new ApiException("Booking not found");
        }
        if (!booking.getStatus().equals("RESERVED")) {
            throw new ApiException("Only RESERVED bookings can be cancelled");
        }
        if (!booking.getUserId().equals(userId)) {
            throw new ApiException("Booking does not belong to the user");
        }

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
    }

    public boolean isWithinAvailabilityWindow(Integer chargerId, LocalDateTime time) {
        for (ChargerAvailability ca : chargerAvailabilityRepository.findChargerAvailabilityByChargerId(chargerId)) {
            if (time.getDayOfWeek().toString().equals(ca.getDayOfWeek())) {
                LocalTime currentTime = time.toLocalTime();
                if (!currentTime.isBefore(ca.getStartTime()) && !currentTime.isAfter(ca.getEndTime())) {
                    return true;
                }
            }
        }
        return false;
    }

    // Scheduled cleanup - runs every 5 minutes
    @Scheduled(fixedRate = 300000)
    public void scheduledCleanupExpiredReservations() {
        Instant cutoff = Instant.now().minusSeconds(HOLD_WINDOW_SECONDS);
        List<Booking> expired = bookingRepository.findBookingByStatusAndCreatedAtBefore("RESERVED", cutoff);

        for (Booking booking : expired) {
            User user = userRepository.findUserById(booking.getUserId());
            user.setNoShowCount(user.getNoShowCount() + 1);
            userRepository.save(user);

            booking.setStatus("CANCELLED");
            bookingRepository.save(booking);
        }

        // Optional: Log the cleanup for monitoring
        if (!expired.isEmpty()) {
            System.out.println("Scheduled cleanup: Cancelled " + expired.size() + " expired reservations");
        }
    }

    // Targeted cleanup for specific car
    private void cleanupExpiredForCar(Integer carId) {
        Instant cutoff = Instant.now().minusSeconds(HOLD_WINDOW_SECONDS);
        List<Booking> expired = bookingRepository.findByCarIdAndStatusAndCreatedAtBefore(carId, "RESERVED", cutoff);

        for (Booking booking : expired) {
            User user = userRepository.findUserById(booking.getUserId());
            user.setNoShowCount(user.getNoShowCount() + 1);
            userRepository.save(user);

            booking.setStatus("CANCELLED");
            bookingRepository.save(booking);
        }
    }

    // Targeted cleanup for specific charger
    private void cleanupExpiredForCharger(Integer chargerId) {
        Instant cutoff = Instant.now().minusSeconds(HOLD_WINDOW_SECONDS);
        List<Booking> expired = bookingRepository.findByChargerIdAndStatusAndCreatedAtBefore(chargerId, "RESERVED", cutoff);

        for (Booking booking : expired) {
            User user = userRepository.findUserById(booking.getUserId());
            user.setNoShowCount(user.getNoShowCount() + 1);
            userRepository.save(user);

            booking.setStatus("CANCELLED");
            bookingRepository.save(booking);
        }
    }
}
