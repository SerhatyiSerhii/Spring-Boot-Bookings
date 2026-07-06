package com.booking.bookings;

/**
 * BookingSearchFilter
 */
public record BookingSearchFilter(
        Long roomId,
        Long userId,
        Integer pageSize,
        Integer pageNumber) {

}
