package com.booking.bookings;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<List<BookingRecord>> getBookings(
            @RequestParam(name = "roomId", required = false) Long roomId,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "pageNumber", required = false) Integer pageNumber) {
        log.info("Get all bookings");
        var filter = new BookingSearchFilter(roomId, userId, pageSize, pageNumber);
        return ResponseEntity.ok(bookingService.searchAllByFilter(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingRecord> getBookingById(@PathVariable("id") Long id) {
        log.info("Get booking for id " + id);

        return ResponseEntity.status(HttpStatus.OK).body(bookingService.getBookingById(id));
    }

    @PostMapping
    public ResponseEntity<BookingRecord> createBookingRecord(
            @RequestBody @Valid BookingRecord bookingToCreate) {
        log.info("Created new booking");

        return ResponseEntity.status(HttpStatus.CREATED).header("Custom-header", "123")
                .body(bookingService.createBookingRecord(bookingToCreate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingRecord> updateBookingById(@PathVariable("id") Long id,
            @RequestBody @Valid BookingRecord bookingToUpdate) {
        log.info("Updated booking by id={}, bookingToUpdate={}", id, bookingToUpdate);

        var updatedBooking = bookingService.updateBookingById(id, bookingToUpdate);

        return ResponseEntity.ok(updatedBooking);
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelBookingById(@PathVariable("id") Long id) {
        log.info("Deleted booking by id={}", id);

        bookingService.cancelBookingById(id);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<BookingRecord> approveBooking(@PathVariable("id") Long id) {
        log.info("Approved booking by id={}", id);

        return ResponseEntity.ok(bookingService.approveBooking(id));
    }
}
