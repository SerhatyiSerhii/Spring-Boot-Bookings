package com.booking.bookings.availability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/booking/availability")
public class BookingAvailabilityController {

    private static final Logger log = LoggerFactory.getLogger(BookingAvailabilityController.class);
    private final BookingAvailabilityService service;

    public BookingAvailabilityController(BookingAvailabilityService service) {
        this.service = service;
    }

    @PostMapping("/check")
    public ResponseEntity<CheckAvailabilityResponse> checkAvailability(
            @RequestBody @Valid CheckAvailabilityRequest request) {
        log.info("Called method checkAvailability: request={}", request);

        boolean isAvailable = service.isBookingAvailable(request.roomId(), request.startDate(), request.endDate());

        var message = isAvailable ? "Room available to booking" : "Room is not available to booking";
        var status = isAvailable ? AvailabilityStatus.AVAILABLE : AvailabilityStatus.RESERVED;

        return ResponseEntity.status(HttpStatus.OK)
                .body(new CheckAvailabilityResponse(message, status));
    }
}
