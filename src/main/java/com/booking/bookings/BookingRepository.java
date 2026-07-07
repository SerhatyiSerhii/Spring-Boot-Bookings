package com.booking.bookings;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    @Modifying
    @Query("""
        UPDATE BookingEntity b
        SET b.status = :status
        where b.id = :id
        """)
    void setStatus(@Param("id") Long id, @Param("status") BookingStatus status);

    @Query("""
        SELECT b.id from BookingEntity b
        where b.roomId = :roomId
        and :startDate < b.endDate
        and b.startDate < :endDate
        and b.status = :status
        """)
    List<Long> findConflictBookingsIds(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") BookingStatus status);

    @Query("""
        SELECT b from BookingEntity b
        where b.roomId = :roomId
        and b.userId = :userId
        """)
    List<BookingEntity> searchAllByFilter(
            @Param("roomId") Long roomId,
            @Param("userId") Long userId,
            Pageable pageable);
}
