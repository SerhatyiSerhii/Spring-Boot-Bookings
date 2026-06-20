package com.booking;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;

@Service
public class BookingService {

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
        BookingEntity foundBookingEntity = repository.findById(id).orElseThrow(()
                -> new EntityNotFoundException("Not found such booking by id " + id));

        BookingStatus foundEntityStatus = foundBookingEntity.getStatus();

        if (foundEntityStatus != BookingStatus.PENDING) {
            throw new IllegalStateException("Can not modify booking: status = " + foundEntityStatus);
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

    public void deleteBookingById(Long id) {
        BookingEntity foundBookingEntity = repository.findById(id).orElseThrow(()
                -> new EntityNotFoundException("Not found such booking by id " + id));

        repository.delete(foundBookingEntity);
    }

    public BookingRecord approveBooking(Long id) {
        BookingEntity foundBookingEntity = repository.findById(id).orElseThrow(()
                -> new EntityNotFoundException("Not found such booking by id " + id));

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
