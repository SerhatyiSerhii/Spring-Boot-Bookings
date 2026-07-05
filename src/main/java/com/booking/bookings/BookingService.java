package com.booking.bookings;

import java.time.LocalDate;
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
    private final BookingMapper mapper;

    public BookingService(BookingRepository repository, BookingMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<BookingRecord> getBookings() {
        List<BookingEntity> allEntities = repository.findAll();
        List<BookingRecord> bookingList = allEntities.stream()
                .map(mapper::convertToBookingRecord)
                .toList();

        return bookingList;
    }

    public BookingRecord getBookingById(Long id) {
        BookingEntity foundBookingEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found such booking by id " + id));

        return mapper.convertToBookingRecord(foundBookingEntity);
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

        return mapper.convertToBookingRecord(savedBookingEntity);
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

        return mapper.convertToBookingRecord(bookingEntityToUpdate);
    }

    @Transactional
    public void cancelBookingById(Long id) {
        BookingEntity foundBookingEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found such booking by id " + id));

        if (foundBookingEntity.getStatus().equals(BookingStatus.APPROVED)) {
            throw new IllegalArgumentException("Can not cancel approved booking. Please contact a manager");
        }

        if (foundBookingEntity.getStatus().equals(BookingStatus.CANCELLED)) {
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

        var isCoonflicted = isBookingConflict(foundBookingEntity.getRoomId(), foundBookingEntity.getStartDate(),
                foundBookingEntity.getEndDate());

        if (isCoonflicted) {
            throw new IllegalStateException("Can not approve conflicted booking.");
        }

        var approvedBookingEntity = new BookingEntity(
                foundBookingEntity.getId(), foundBookingEntity.getUserId(), foundBookingEntity.getRoomId(),
                foundBookingEntity.getStartDate(), foundBookingEntity.getEndDate(), BookingStatus.APPROVED);

        repository.save(approvedBookingEntity);

        return mapper.convertToBookingRecord(approvedBookingEntity);
    }

    public boolean isBookingConflict(
            Long roomId,
            LocalDate startDate,
            LocalDate endDate) {

        List<Long> conflictingIds = repository.findConflictBookingsIds(roomId, startDate, endDate,
                BookingStatus.APPROVED);

        if (conflictingIds.isEmpty()) {
            return false;
        }

        log.info("Conflict with ids={}", conflictingIds);

        return true;
    }

}
