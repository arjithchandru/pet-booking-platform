package com.petcare.booking.domain.entity;

import com.petcare.booking.domain.enums.ServiceStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "services")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ServiceStatus status = ServiceStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}