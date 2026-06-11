package com.booking;

import java.time.LocalDate;

public record BookingRecord(
        Long id,
        Long userId,
        Long roomId,
        LocalDate starDate,
        LocalDate endDate,
        BookingStatus status
        ) {

}
