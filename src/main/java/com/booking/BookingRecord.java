package com.booking;

import java.time.LocalDate;

public record BookingRecord(
        Long id,
        Long userId,
        Long roomId,
        LocalDate startDate,
        LocalDate endDate,
        BookingStatus status
        ) {

}
