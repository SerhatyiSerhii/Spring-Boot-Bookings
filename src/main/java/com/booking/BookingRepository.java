package com.booking;

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
}
