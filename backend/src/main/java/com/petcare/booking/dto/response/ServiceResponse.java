package com.petcare.booking.dto.response;

import com.petcare.booking.domain.entity.Service;
import com.petcare.booking.domain.enums.ServiceStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record ServiceResponse(
        UUID id,
        String name,
        String description,
        String category,
        Integer durationMinutes,
        BigDecimal price,
        ServiceStatus status
) {
    public static ServiceResponse fromEntity(Service s) {
        return new ServiceResponse(
                s.getId(),
                s.getName(),
                s.getDescription(),
                s.getCategory(),
                s.getDurationMinutes(),
                s.getPrice(),
                s.getStatus()
        );
    }
}