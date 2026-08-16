package com.petcare.booking.controller;

import com.petcare.booking.dto.request.CreateBookingRequest;
import com.petcare.booking.dto.response.BookingResponse;
import com.petcare.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings & Calendar")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a new booking directly from the calendar")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }

    @GetMapping
    @Operation(summary = "Get bookings in date range for calendar rendering")
    public ResponseEntity<List<BookingResponse>> getBookings(
            @RequestParam Instant from,
            @RequestParam Instant to) {
        return ResponseEntity.ok(bookingService.getBookingsInRange(from, to));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking details by ID")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking (releases the slot back to available)")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }
}