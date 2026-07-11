package com.booking.bookings.availability;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.booking.bookings.BookingRepository;
import com.booking.bookings.BookingService;
import com.booking.bookings.BookingStatus;

@Service
public class BookingAvailabilityService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository repository;

    public BookingAvailabilityService(BookingRepository repository) {
        this.repository = repository;
    }

    public boolean isBookingAvailable(
            Long roomId,
            LocalDate startDate,
            LocalDate endDate) {

        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("End date can't be before start date");
        }

        List<Long> conflictingIds = repository.findConflictBookingsIds(roomId, startDate, endDate,
                BookingStatus.APPROVED);

        if (conflictingIds.isEmpty()) {
            return true;
        }

        log.info("Conflict with ids={}", conflictingIds);

        return false;
    }
}
