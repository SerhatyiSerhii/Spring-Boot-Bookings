package com.booking.bookings.availability;

public record CheckAvailabilityResponse(
    String message,
    AvailabilityStatus status
) {
}
