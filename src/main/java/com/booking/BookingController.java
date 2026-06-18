package com.booking;

import java.util.List;
import java.util.NoSuchElementException;

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
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<List<BookingRecord>> getBookings() {
        log.info("Get all bookings");
        return ResponseEntity.ok(bookingService.getBookings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingRecord> getBookingById(@PathVariable("id") Long id) {
        log.info("Get booking for id " + id);

        try {
            return ResponseEntity.status(HttpStatus.OK).body(bookingService.getBookingById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }

    @PostMapping
    public ResponseEntity<BookingRecord> createBookingRecord(
            @RequestBody BookingRecord bookingToCreate) {
        log.info("Created new booking");

        return ResponseEntity.status(HttpStatus.CREATED).header("Custom-header", "123")
                .body(bookingService.createBookingRecord(bookingToCreate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingRecord> updateBookingById(@PathVariable("id") Long id,
            @RequestBody BookingRecord bookingToUpdate) {
        log.info("Updated booking by id={}, bookingToUpdate={}", id, bookingToUpdate);

        var updatedBooking = bookingService.updateBookingById(id, bookingToUpdate);

        try {
            return ResponseEntity.ok(updatedBooking);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookingById(@PathVariable("id") Long id) {
        log.info("Deleted booking by id={}", id);

        try {
            bookingService.deleteBookingById(id);

            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<BookingRecord> approveBooking(@PathVariable("id") Long id) {
        log.info("Approved booking by id={}", id);
        try {
            return ResponseEntity.ok(bookingService.approveBooking(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

    }
}
