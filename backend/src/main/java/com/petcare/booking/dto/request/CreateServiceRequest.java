package com.petcare.booking.dto.request;

import com.petcare.booking.domain.enums.ServiceStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CreateServiceRequest(
        @NotBlank(message = "Service name is required")
        String name,

        String description,

        @NotBlank(message = "Category is required")
        String category,

        @NotNull(message = "Duration in minutes is required")
        @Min(value = 5, message = "Duration must be at least 5 minutes")
        Integer durationMinutes,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
        BigDecimal price,

        ServiceStatus status
) {}