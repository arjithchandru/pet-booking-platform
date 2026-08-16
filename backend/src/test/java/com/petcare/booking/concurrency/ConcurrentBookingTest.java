package com.petcare.booking.concurrency;

import com.petcare.booking.domain.entity.Tenant;
import com.petcare.booking.dto.request.CreateBookingRequest;
import com.petcare.booking.exception.BookingConflictException;
import com.petcare.booking.repository.BookingRepository;
import com.petcare.booking.repository.TenantRepository;
import com.petcare.booking.security.TenantContext;
import com.petcare.booking.service.BookingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ConcurrentBookingTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private Tenant tenant;
    private UUID serviceId;
    private UUID staffId;
    private Instant requestedSlot;

    @BeforeEach
    void setUp() {
        tenant = tenantRepository.findById(UUID.fromString("11111111-1111-1111-1111-111111111111")).orElseThrow();
        serviceId = UUID.fromString("11111111-2222-0000-0000-000000000001"); // Full Grooming (90 mins)
        staffId = UUID.fromString("11111111-3333-0000-0000-000000000001");   // John Doe

        // Compute 10:00 AM local time in the tenant's actual timezone on a future Monday
        ZoneId zone = ZoneId.of(tenant.getTimezone() != null ? tenant.getTimezone() : "UTC");
        ZonedDateTime localMonday10Am = ZonedDateTime.of(2026, 9, 14, 10, 0, 0, 0, zone);
        requestedSlot = localMonday10Am.toInstant();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Simultaneous booking attempts for the same staff slot allow at most one booking to succeed")
    void givenConcurrentBookings_onlyOneSucceeds() throws Exception {
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        Callable<Void> bookingTask = () -> {
            startLatch.await(); // Synchronize both threads to fire at the exact same instant
            TenantContext.setCurrentTenant(tenant);
            try {
                bookingService.createBooking(new CreateBookingRequest(
                        serviceId,
                        staffId,
                        requestedSlot,
                        "Customer " + Thread.currentThread().getName(),
                        "Pet " + Thread.currentThread().getName()
                ));
                successCount.incrementAndGet();
            } catch (BookingConflictException | DataIntegrityViolationException ex) {
                conflictCount.incrementAndGet();
            } finally {
                TenantContext.clear();
            }
            return null;
        };

        Future<Void> f1 = executor.submit(bookingTask);
        Future<Void> f2 = executor.submit(bookingTask);

        startLatch.countDown(); // Fire both threads simultaneously

        f1.get();
        f2.get();
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(1);
    }
}