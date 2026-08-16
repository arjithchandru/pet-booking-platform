package com.petcare.booking.dto.request;

import com.petcare.booking.domain.enums.StaffStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

public record UpdateStaffRequest(
        @NotBlank(message = "Staff name is required")
        String name,

        @Email(message = "Invalid email format")
        String email,

        @NotNull(message = "Status is required")
        StaffStatus status,

        Set<UUID> serviceIds
) {}