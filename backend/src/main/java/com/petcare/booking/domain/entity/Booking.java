package com.petcare.booking.domain.entity;

import com.petcare.booking.domain.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(name = "customer_name", nullable = false, length = 150)
    private String customerName;

    @Column(name = "pet_name", length = 100)
    private String petName;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}