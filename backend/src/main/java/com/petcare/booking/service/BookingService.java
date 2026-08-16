package com.petcare.booking.service;

import com.petcare.booking.domain.entity.Booking;
import com.petcare.booking.domain.entity.Service;
import com.petcare.booking.domain.entity.Staff;
import com.petcare.booking.domain.enums.BookingStatus;
import com.petcare.booking.domain.enums.ServiceStatus;
import com.petcare.booking.dto.request.CreateBookingRequest;
import com.petcare.booking.dto.response.BookingResponse;
import com.petcare.booking.exception.BookingConflictException;
import com.petcare.booking.exception.ResourceNotFoundException;
import com.petcare.booking.repository.BookingRepository;
import com.petcare.booking.repository.ServiceRepository;
import com.petcare.booking.repository.StaffRepository;
import com.petcare.booking.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final StaffRepository staffRepository;
    private final ServiceRepository serviceRepository;
    private final AvailabilityService availabilityService;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BookingResponse createBooking(CreateBookingRequest req) {
        UUID tenantId = TenantContext.getCurrentTenant().getId();

        // 1. Acquire pessimistic write lock on the staff record
        Staff staff = staffRepository.findByIdAndTenantIdWithLock(req.staffId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found or invalid tenant"));

        Service service = serviceRepository.findByIdAndTenantId(req.serviceId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        if (service.getStatus() != ServiceStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot book an inactive service");
        }

        // 2. Re-verify availability inside the lock transaction
        boolean isEligible = availabilityService.getEligibleStaff(service.getId(), req.startAt())
                .stream().anyMatch(s -> s.id().equals(staff.getId()));

        if (!isEligible) {
            throw new BookingConflictException("The selected slot is no longer available for this staff member.");
        }

        Instant endAt = req.startAt().plus(service.getDurationMinutes(), ChronoUnit.MINUTES);

        // 3. Persist booking
        Booking booking = Booking.builder()
                .tenantId(tenantId)
                .service(service)
                .staff(staff)
                .customerName(req.customerName())
                .petName(req.petName())
                .startAt(req.startAt())
                .endAt(endAt)
                .status(BookingStatus.CONFIRMED)
                .build();

        return BookingResponse.fromEntity(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsInRange(Instant from, Instant to) {
        UUID tenantId = TenantContext.getCurrentTenant().getId();
        return bookingRepository.findBookingsInRange(tenantId, from, to).stream()
                .map(BookingResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenant().getId();
        return bookingRepository.findByIdAndTenantId(id, tenantId)
                .map(BookingResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    @Transactional
    public BookingResponse cancelBooking(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenant().getId();
        Booking booking = bookingRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        booking.setStatus(BookingStatus.CANCELLED);
        return BookingResponse.fromEntity(bookingRepository.save(booking));
    }
}