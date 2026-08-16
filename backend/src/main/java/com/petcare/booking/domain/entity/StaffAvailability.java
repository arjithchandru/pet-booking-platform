package com.petcare.booking.domain.entity;

import com.petcare.booking.domain.enums.AvailabilityType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "staff_availability")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StaffAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "staff_id", nullable = false)
    private UUID staffId;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek; // 1 = Mon, 7 = Sun

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AvailabilityType type;
}