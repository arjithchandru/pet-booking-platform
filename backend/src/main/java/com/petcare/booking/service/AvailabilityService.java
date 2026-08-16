package com.petcare.booking.service;

import com.petcare.booking.domain.entity.Service;
import com.petcare.booking.domain.entity.Staff;
import com.petcare.booking.domain.entity.StaffAvailability;
import com.petcare.booking.domain.entity.Tenant;
import com.petcare.booking.domain.enums.AvailabilityType;
import com.petcare.booking.domain.enums.ServiceStatus;
import com.petcare.booking.dto.response.StaffResponse;
import com.petcare.booking.exception.ResourceNotFoundException;
import com.petcare.booking.repository.BookingRepository;
import com.petcare.booking.repository.ServiceRepository;
import com.petcare.booking.repository.StaffAvailabilityRepository;
import com.petcare.booking.repository.StaffRepository;
import com.petcare.booking.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final StaffRepository staffRepository;
    private final ServiceRepository serviceRepository;
    private final StaffAvailabilityRepository availabilityRepository;
    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public List<StaffResponse> getEligibleStaff(UUID serviceId, Instant requestedStart) {
        Tenant tenant = TenantContext.getCurrentTenant();
        Service service = serviceRepository.findByIdAndTenantId(serviceId, tenant.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        if (service.getStatus() != ServiceStatus.ACTIVE) {
            return List.of();
        }

        ZoneId zoneId = ZoneId.of(tenant.getTimezone());
        ZonedDateTime localStart = requestedStart.atZone(zoneId);
        Instant requestedEnd = requestedStart.plus(service.getDurationMinutes(), ChronoUnit.MINUTES);
        ZonedDateTime localEnd = requestedEnd.atZone(zoneId);

        DayOfWeek dayOfWeek = localStart.getDayOfWeek();
        LocalTime slotStartTime = localStart.toLocalTime();
        LocalTime slotEndTime = localEnd.toLocalTime();

        // 1. Fetch active staff assigned to this service
        List<Staff> qualifiedStaff = staffRepository.findActiveStaffByService(tenant.getId(), serviceId);

        return qualifiedStaff.stream().filter(staff -> {
            // 2. Must fit fully inside working hours
            List<StaffAvailability> workingHours = availabilityRepository.findByStaffAndDayAndType(
                    tenant.getId(), staff.getId(), dayOfWeek.getValue(), AvailabilityType.WORKING_HOURS);

            boolean withinWorkingHours = workingHours.stream().anyMatch(w ->
                    !slotStartTime.isBefore(w.getStartTime()) && !slotEndTime.isAfter(w.getEndTime())
            );
            if (!withinWorkingHours) return false;

            // 3. Must NOT overlap any break window [slotStart, slotEnd) && [breakStart, breakEnd)
            List<StaffAvailability> breaks = availabilityRepository.findByStaffAndDayAndType(
                    tenant.getId(), staff.getId(), dayOfWeek.getValue(), AvailabilityType.BREAK);

            boolean overlapsBreak = breaks.stream().anyMatch(b ->
                    slotStartTime.isBefore(b.getEndTime()) && slotEndTime.isAfter(b.getStartTime())
            );
            if (overlapsBreak) return false;

            // 4. Must NOT overlap any existing confirmed booking
            boolean hasBookingOverlap = bookingRepository.existsOverlappingBooking(
                    tenant.getId(), staff.getId(), requestedStart, requestedEnd);

            return !hasBookingOverlap;
        }).map(StaffResponse::fromEntity).toList();
    }
}