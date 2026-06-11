package com.booking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

@Service
public class BookingService {
    // private final Map<Long, BookingRecord> bookingMap = Map.of(
    // 1L, new BookingRecord(
    // 1L,
    // 101L,
    // 41L,
    // LocalDate.now(),
    // LocalDate.now().plusDays(5),
    // BookingStatus.APPROVED
    // ),
    // 2L, new BookingRecord(
    // 2L,
    // 102L,
    // 42L,
    // LocalDate.now(),
    // LocalDate.now().plusDays(5),
    // BookingStatus.APPROVED
    // ),
    // 3L, new BookingRecord(
    // 3L,
    // 103L,
    // 43L,
    // LocalDate.now(),
    // LocalDate.now().plusDays(5),
    // BookingStatus.APPROVED
    // )
    // );

    private final Map<Long, BookingRecord> bookingMap;
    private final AtomicLong idCounder;

    public BookingService() {
        bookingMap = new HashMap<>();
        idCounder = new AtomicLong();
    }

    public List<BookingRecord> getBookings() {
        return bookingMap.values().stream().toList();
    }

    public BookingRecord getBookingById(Long id) {
        if (!bookingMap.containsKey(id)) {
            throw new NoSuchElementException("Not found such booking by id " + id);
        }

        return bookingMap.get(id);
    }

    public BookingRecord createBookingRecord(BookingRecord bookingToCreate) {

        if (bookingToCreate.id() != null) {
            throw new IllegalArgumentException("Id should be empty");
        }

        if (bookingToCreate.status() != null) {
            throw new IllegalArgumentException("Status should be empty");
        }

        var newBooking = new BookingRecord(
                idCounder.incrementAndGet(),
                bookingToCreate.userId(),
                bookingToCreate.roomId(),
                bookingToCreate.starDate(),
                bookingToCreate.endDate(),
                BookingStatus.PENDING);

        bookingMap.put(newBooking.id(), newBooking);

        return newBooking;
    }

}
