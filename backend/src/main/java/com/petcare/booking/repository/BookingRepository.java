package com.petcare.booking.repository;

import com.petcare.booking.domain.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Optional<Booking> findByIdAndTenantId(UUID id, UUID tenantId);

    // Standard interval overlap for calendar fetching: startAt < :to AND endAt > :from
    @Query("""
        SELECT b FROM Booking b 
        JOIN FETCH b.service 
        JOIN FETCH b.staff 
        WHERE b.tenantId = :tenantId 
          AND b.status = 'CONFIRMED'
          AND b.startAt < :to 
          AND b.endAt > :from
    """)
    List<Booking> findBookingsInRange(
            @Param("tenantId") UUID tenantId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b 
        WHERE b.tenantId = :tenantId 
          AND b.staff.id = :staffId 
          AND b.status = 'CONFIRMED'
          AND b.startAt < :endAt 
          AND b.endAt > :startAt
    """)
    boolean existsOverlappingBooking(
            @Param("tenantId") UUID tenantId,
            @Param("staffId") UUID staffId,
            @Param("startAt") Instant startAt,
            @Param("endAt") Instant endAt
    );
}