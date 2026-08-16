package com.petcare.booking.dto.response;

import com.petcare.booking.domain.entity.StaffAvailability;
import com.petcare.booking.domain.enums.AvailabilityType;
import lombok.Builder;

import java.time.LocalTime;
import java.util.UUID;

@Builder
public record StaffAvailabilityResponse(
        UUID id,
        UUID staffId,
        Integer dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        AvailabilityType type
) {
    public static StaffAvailabilityResponse fromEntity(StaffAvailability a) {
        return new StaffAvailabilityResponse(
                a.getId(),
                a.getStaffId(),
                a.getDayOfWeek(),
                a.getStartTime(),
                a.getEndTime(),
                a.getType()
        );
    }
}