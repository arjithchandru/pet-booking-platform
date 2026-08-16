package com.petcare.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record CreateBookingRequest(
        @NotNull(message = "Service ID is required")
        UUID serviceId,

        @NotNull(message = "Staff ID is required")
        UUID staffId,

        @NotNull(message = "Start time is required")
        Instant startAt,

        @NotBlank(message = "Customer name is required")
        String customerName,

        String petName
) {}