package com.booking.bookings;

import org.springframework.stereotype.Component;

@Component
public class BookingMapper {
    public BookingRecord convertToBookingRecord(BookingEntity entity) {
        return new BookingRecord(entity.getId(), entity.getUserId(), entity.getRoomId(), entity.getStartDate(),
                entity.getEndDate(), entity.getStatus());
    }

    public BookingEntity convertToBookingEntity(BookingRecord record) {
        return new BookingEntity(record.id(), record.userId(), record.roomId(),
                record.startDate(), record.endDate(), record.status());
    }
}
