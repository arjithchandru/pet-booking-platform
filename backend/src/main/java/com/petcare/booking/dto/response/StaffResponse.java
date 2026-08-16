package com.petcare.booking.dto.response;

import com.petcare.booking.domain.entity.Staff;
import com.petcare.booking.domain.enums.StaffStatus;
import lombok.Builder;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
public record StaffResponse(
        UUID id,
        String name,
        String email,
        StaffStatus status,
        Set<UUID> qualifiedServiceIds
) {
    public static StaffResponse fromEntity(Staff s) {
        return new StaffResponse(
                s.getId(),
                s.getName(),
                s.getEmail(),
                s.getStatus(),
                s.getServices() != null
                        ? s.getServices().stream().map(com.petcare.booking.domain.entity.Service::getId).collect(Collectors.toSet())
                        : Set.of()
        );
    }
}