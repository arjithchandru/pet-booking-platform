package com.petcare.booking.dto.request;

import com.petcare.booking.domain.enums.StaffStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record CreateStaffRequest(
        @NotBlank(message = "Staff name is required")
        String name,

        @NotBlank(message = "Staff email is required")
        @Email(message = "Email format is invalid")
        String email,

        StaffStatus status,

        List<UUID> serviceIds
) {}