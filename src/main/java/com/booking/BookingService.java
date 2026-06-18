package com.booking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;

@Service
public class BookingService {

    private final Map<Long, BookingRecord> bookingMap;
    private final AtomicLong idCounder;
    private final BookingRepository repository;

    private BookingRecord convertToBookingRecord(BookingEntity entity) {
        return new BookingRecord(entity.getId(), entity.getUserId(), entity.getRoomId(), entity.getStartDate(),
                entity.getEndDate(), entity.getStatus());
    }

    public BookingService(BookingRepository repository) {
        this.repository = repository;
        bookingMap = new HashMap<>();
        idCounder = new AtomicLong();
    }

    public List<BookingRecord> getBookings() {
        List<BookingEntity> allEntities = repository.findAll();
        List<BookingRecord> bookingList = allEntities.stream()
                .map(this::convertToBookingRecord)
                .toList();

        return bookingList;
    }

    public BookingRecord getBookingById(Long id) {
        BookingEntity foundBookingEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found such booking by id " + id));

        return convertToBookingRecord(foundBookingEntity);
    }

    public BookingRecord createBookingRecord(BookingRecord bookingToCreate) {

        if (bookingToCreate.id() != null) {
            throw new IllegalArgumentException("Id should be empty");
        }

        if (bookingToCreate.status() != null) {
            throw new IllegalArgumentException("Status should be empty");
        }

        var newBookingEntity = new BookingEntity(
                null,
                bookingToCreate.userId(),
                bookingToCreate.roomId(),
                bookingToCreate.startDate(),
                bookingToCreate.endDate(),
                BookingStatus.PENDING);

        var savedBookingEntity = repository.save(newBookingEntity);

        return convertToBookingRecord(savedBookingEntity);
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
                bookingToUpdate.startDate(),
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

    public BookingRecord approveBooking(Long id) {
        if (!bookingMap.containsKey(id)) {
            throw new NoSuchElementException("Not found such booking by id " + id);
        }

        var bookingToApprove = bookingMap.get(id);

        if (bookingToApprove.status() != BookingStatus.PENDING) {
            throw new IllegalStateException("Can not approve booking.");
        }

        var isCoonflicted = isBookingConflict(bookingToApprove);

        if (isCoonflicted) {
            throw new IllegalStateException("Can not approve conflicted booking.");
        }

        var approvedBooking = new BookingRecord(
                bookingToApprove.id(), bookingToApprove.userId(), bookingToApprove.roomId(),
                bookingToApprove.startDate(), bookingToApprove.endDate(), BookingStatus.APPROVED);

        bookingMap.put(bookingToApprove.id(), approvedBooking);

        return approvedBooking;
    }

    public boolean isBookingConflict(BookingRecord booking) {

        for (BookingRecord existingBooking : bookingMap.values()) {
            if (existingBooking.id().equals(booking.id())) {
                continue;
            }

            if (!booking.roomId().equals(existingBooking.roomId())) {
                continue;
            }

            if (!existingBooking.status().equals(BookingStatus.APPROVED)) {
                continue;
            }

            if (booking.startDate().isBefore(existingBooking.endDate())
                    && existingBooking.startDate().isBefore(booking.endDate())) {

                return true;
            }
        }

        return false;
    }

}
