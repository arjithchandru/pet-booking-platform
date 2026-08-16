package com.petcare.booking.dto.request;

import com.petcare.booking.domain.enums.AvailabilityType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record AvailabilityWindowRequest(
        @NotNull(message = "Day of week is required")
        @Min(1) @Max(7)
        Integer dayOfWeek, // 1 = Mon, 7 = Sun

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime,

        @NotNull(message = "Availability type is required")
        AvailabilityType type
) {}