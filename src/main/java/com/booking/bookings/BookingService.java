package com.booking.bookings;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.booking.bookings.availability.BookingAvailabilityService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository repository;
    private final BookingMapper mapper;
    private final BookingAvailabilityService availabilityService;
    @Value("${app.defaul.page.size}")
    private int defaultPageSize;
    @Value("${app.defaul.page.number}")
    private int defaultPageNumber;

    public BookingService(BookingRepository repository, BookingMapper mapper, BookingAvailabilityService availabilityService) {
        this.repository = repository;
        this.mapper = mapper;
        this.availabilityService = availabilityService;
    }

    public List<BookingRecord> searchAllByFilter(BookingSearchFilter filter) {
        var filteredPageSize = filter.pageSize();
        var filteredPageNumber = filter.pageNumber();

        int pageSize = filteredPageSize != null ? filteredPageSize : defaultPageSize;
        int pageNumber = filteredPageNumber != null ? filteredPageNumber : defaultPageNumber;

        var pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

        List<BookingEntity> allEntities = repository.searchAllByFilter(filter.roomId(), filter.userId(), pageable);

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

        var newBookingEntity = mapper.convertToBookingEntity(bookingToCreate);
        newBookingEntity.setId(null);
        newBookingEntity.setStatus(BookingStatus.PENDING);

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

        var bookingEntityToUpdate = mapper.convertToBookingEntity(bookingToUpdate);
        bookingEntityToUpdate.setId(foundBookingEntity.getId());
        bookingEntityToUpdate.setStatus(BookingStatus.PENDING);

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

        var isAvailAbleToApprove = availabilityService.isBookingAvailable(foundBookingEntity.getRoomId(), foundBookingEntity.getStartDate(),
                foundBookingEntity.getEndDate());

        if (!isAvailAbleToApprove) {
            throw new IllegalStateException("Can not approve conflicted booking.");
        }

        var approvedBookingEntity = new BookingEntity(
                foundBookingEntity.getId(), foundBookingEntity.getUserId(), foundBookingEntity.getRoomId(),
                foundBookingEntity.getStartDate(), foundBookingEntity.getEndDate(), BookingStatus.APPROVED);

        repository.save(approvedBookingEntity);

        return mapper.convertToBookingRecord(approvedBookingEntity);
    }
}
