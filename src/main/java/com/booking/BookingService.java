package com.booking;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository repository;

    private BookingRecord convertToBookingRecord(BookingEntity entity) {
        return new BookingRecord(entity.getId(), entity.getUserId(), entity.getRoomId(), entity.getStartDate(),
                entity.getEndDate(), entity.getStatus());
    }

    public BookingService(BookingRepository repository) {
        this.repository = repository;
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

        if (bookingToCreate.status() != null) {
            throw new IllegalArgumentException("Status should be empty");
        }

        if (!bookingToCreate.endDate().isAfter(bookingToCreate.startDate())) {
            throw new IllegalArgumentException("End date can't be before start date");
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
        BookingEntity foundBookingEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found such booking by id " + id));

        BookingStatus foundEntityStatus = foundBookingEntity.getStatus();

        if (foundEntityStatus != BookingStatus.PENDING) {
            throw new IllegalStateException("Can not modify booking: status = " + foundEntityStatus);
        }

        if (!bookingToUpdate.endDate().isAfter(bookingToUpdate.startDate())) {
            throw new IllegalArgumentException("End date can't be before start date");
        }

        var bookingEntityToUpdate = new BookingEntity(
                foundBookingEntity.getId(),
                bookingToUpdate.userId(),
                bookingToUpdate.roomId(),
                bookingToUpdate.startDate(),
                bookingToUpdate.endDate(),
                BookingStatus.PENDING);

        repository.save(bookingEntityToUpdate);

        return convertToBookingRecord(bookingEntityToUpdate);
    }

    @Transactional
    public void cancelBookingById(Long id) {
        BookingEntity foundBookingEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found such booking by id " + id));

        if(foundBookingEntity.getStatus().equals(BookingStatus.APPROVED)) {
            throw new IllegalArgumentException("Can not cancel approved booking. Please contact a manager");
        }

        if(foundBookingEntity.getStatus().equals(BookingStatus.CANCELLED)) {
            throw new IllegalArgumentException("Booking's already been canceled");
        }

        repository.setStatus(id, BookingStatus.CANCELLED);

        log.info("Successfully cancelled booking id={}", id);
    }

    public BookingRecord approveBooking(Long id) {
        BookingEntity foundBookingEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found such booking by id " + id));

        BookingStatus foundEntityStatus = foundBookingEntity.getStatus();

        if (foundEntityStatus != BookingStatus.PENDING) {
            throw new IllegalStateException("Can not approve booking.");
        }

        var isCoonflicted = isBookingConflict(foundBookingEntity);

        if (isCoonflicted) {
            throw new IllegalStateException("Can not approve conflicted booking.");
        }

        var approvedBookingEntity = new BookingEntity(
                foundBookingEntity.getId(), foundBookingEntity.getUserId(), foundBookingEntity.getRoomId(),
                foundBookingEntity.getStartDate(), foundBookingEntity.getEndDate(), BookingStatus.APPROVED);

        repository.save(approvedBookingEntity);

        return convertToBookingRecord(approvedBookingEntity);
    }

    public boolean isBookingConflict(BookingEntity booking) {

        for (BookingEntity existingBookingEntity : repository.findAll()) {
            if (existingBookingEntity.getId().equals(booking.getId())) {
                continue;
            }

            if (!booking.getRoomId().equals(existingBookingEntity.getRoomId())) {
                continue;
            }

            if (!existingBookingEntity.getStatus().equals(BookingStatus.APPROVED)) {
                continue;
            }

            if (booking.getStartDate().isBefore(existingBookingEntity.getEndDate())
                    && existingBookingEntity.getStartDate().isBefore(booking.getEndDate())) {

                return true;
            }
        }

        return false;
    }

}
