package com.booking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

@Service
public class BookingService {
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

    public BookingRecord updateBookingById(Long id, BookingRecord bookingToUpdate) {
        if (!bookingMap.containsKey(id)) {
            throw new NoSuchElementException("Not found such booking by id " + id);
        }

        var booking = bookingMap.get(id);

        if (booking.status() != BookingStatus.PENDING) {
            throw new IllegalStateException("Can not modify booking: status = " + booking.status());
        }

        var updatedBooking = new BookingRecord(
                booking.id(),
                bookingToUpdate.userId(),
                bookingToUpdate.roomId(),
                bookingToUpdate.starDate(),
                bookingToUpdate.endDate(),
                BookingStatus.PENDING);

        bookingMap.put(booking.id(), updatedBooking);

        return updatedBooking;
    }

    public void deleteBookingById(Long id) {
        if (!bookingMap.containsKey(id)) {
            throw new NoSuchElementException("Not found such booking by id " + id);
        }

        bookingMap.remove(id);
    }

}
