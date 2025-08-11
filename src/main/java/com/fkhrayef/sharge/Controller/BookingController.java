package com.fkhrayef.sharge.Controller;

import com.fkhrayef.sharge.Api.ApiResponse;
import com.fkhrayef.sharge.Service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllBookings() {
        return ResponseEntity.status(HttpStatus.OK).body(bookingService.getAllBookings());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserBookings(@PathVariable Integer userId) {
        return ResponseEntity.status(HttpStatus.OK).body(bookingService.getUserBookings(userId));
    }

    @PostMapping("/reserve/{userId}/{carId}/{chargerId}")
    public ResponseEntity<?> reserveCharger(@PathVariable("userId") Integer userId,
                                            @PathVariable("carId") Integer carId,
                                            @PathVariable("chargerId") Integer chargerId,
                                            @RequestParam Double latitude,
                                            @RequestParam Double longitude) {
        
        bookingService.reserveCharger(userId, carId, chargerId, latitude, longitude);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("Charger reserved successfully"));
    }

    @PutMapping("/{bookingId}/confirm-arrival")
    public ResponseEntity<?> confirmArrival(@PathVariable Integer bookingId,
                                            @RequestParam Integer userId) {
        bookingService.confirmArrival(userId, bookingId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("Arrival confirmed successfully"));
    }

    @PutMapping("/{bookingId}/end-session")
    public ResponseEntity<?> endSession(@PathVariable Integer bookingId,
                                        @RequestParam Integer userId) {
        bookingService.endSession(userId, bookingId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("Session ended successfully"));
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> cancelReservation(@PathVariable Integer bookingId,
                                               @RequestParam Integer userId) {
        bookingService.cancelReservation(userId, bookingId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("Reservation cancelled successfully"));
    }
}
