package com.booking;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/")
    public List<BookingRecord> getBookings() {
        log.info("Get all bookings");
        return bookingService.getBookings();
    }

    @GetMapping("/{id}")
    public BookingRecord getBookingById(@PathVariable("id") Long id) {
        log.info("Get booking for userId " + id);
        return bookingService.getBookingById(id);
    }

    @PostMapping("/")
    public BookingRecord creatBookingRecord(
            @RequestBody BookingRecord bookingToCreate) {
        log.info("Created new booking");
        return bookingService.createBookingRecord(bookingToCreate);
    }
}
