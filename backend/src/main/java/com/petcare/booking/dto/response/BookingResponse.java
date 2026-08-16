package com.petcare.booking.dto.response;

import com.petcare.booking.domain.entity.Booking;
import com.petcare.booking.domain.enums.BookingStatus;
import java.time.Instant;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        UUID serviceId,
        String serviceName,
        UUID staffId,
        String staffName,
        String customerName,
        String petName,
        Instant startAt,
        Instant endAt,
        BookingStatus status
) {
    public static BookingResponse fromEntity(Booking b) {
        return new BookingResponse(
                b.getId(),
                b.getService().getId(),
                b.getService().getName(),
                b.getStaff().getId(),
                b.getStaff().getName(),
                b.getCustomerName(),
                b.getPetName(),
                b.getStartAt(),
                b.getEndAt(),
                b.getStatus()
        );
    }
}
