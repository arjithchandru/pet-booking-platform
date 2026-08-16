package com.petcare.booking.dto.response;

import com.petcare.booking.domain.entity.User;
import com.petcare.booking.domain.enums.UserRole;
import java.util.UUID;

public record UserContextResponse(
        UUID userId,
        String email,
        UserRole role,
        UUID tenantId,
        String tenantName,
        String timezone
) {
    public static UserContextResponse fromEntity(User user) {
        return new UserContextResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getTenant().getId(),
                user.getTenant().getName(),
                user.getTenant().getTimezone()
        );
    }
}