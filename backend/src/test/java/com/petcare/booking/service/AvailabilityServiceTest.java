package com.petcare.booking.service;

import com.petcare.booking.domain.entity.Service;
import com.petcare.booking.domain.entity.Staff;
import com.petcare.booking.domain.entity.StaffAvailability;
import com.petcare.booking.domain.entity.Tenant;
import com.petcare.booking.domain.enums.AvailabilityType;
import com.petcare.booking.domain.enums.ServiceStatus;
import com.petcare.booking.domain.enums.StaffStatus;
import com.petcare.booking.dto.response.StaffResponse;
import com.petcare.booking.exception.ResourceNotFoundException;
import com.petcare.booking.repository.BookingRepository;
import com.petcare.booking.repository.ServiceRepository;
import com.petcare.booking.repository.StaffAvailabilityRepository;
import com.petcare.booking.repository.StaffRepository;
import com.petcare.booking.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private StaffAvailabilityRepository availabilityRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    private UUID tenantId;
    private UUID serviceId;
    private UUID staffId;
    private Service service;
    private Staff staff;

    // Fixed UTC instant: Monday, August 17, 2026 at 10:00:00 UTC (Day 1)
    private final Instant testStartAt = Instant.parse("2026-08-17T10:00:00Z");
    private final Instant testEndAt = testStartAt.plus(60, ChronoUnit.MINUTES);

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        serviceId = UUID.randomUUID();
        staffId = UUID.randomUUID();

        Tenant tenant = Tenant.builder()
                .id(tenantId)
                .name("Happy Paws")
                .timezone("UTC")
                .build();

        TenantContext.setCurrentTenant(tenant);

        service = Service.builder()
                .id(serviceId)
                .tenantId(tenantId)
                .name("Full Grooming")
                .durationMinutes(60)
                .status(ServiceStatus.ACTIVE)
                .build();

        staff = Staff.builder()
                .id(staffId)
                .tenantId(tenantId)
                .name("Alex Green")
                .email("alex@happypaws.test")
                .status(StaffStatus.ACTIVE)
                .services(Set.of(service))
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should return eligible staff when all 5 availability conditions pass")
    void shouldReturnEligibleStaff_WhenAllConditionsPass() {
        StaffAvailability workWindow = StaffAvailability.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .staffId(staffId)
                .dayOfWeek(1)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .type(AvailabilityType.WORKING_HOURS)
                .build();

        when(serviceRepository.findByIdAndTenantId(serviceId, tenantId)).thenReturn(Optional.of(service));
        when(staffRepository.findActiveStaffByService(tenantId, serviceId)).thenReturn(List.of(staff));
        when(availabilityRepository.findByStaffAndDayAndType(eq(tenantId), eq(staffId), eq(1), eq(AvailabilityType.WORKING_HOURS)))
                .thenReturn(List.of(workWindow));
        when(availabilityRepository.findByStaffAndDayAndType(eq(tenantId), eq(staffId), eq(1), eq(AvailabilityType.BREAK)))
                .thenReturn(List.of());
        when(bookingRepository.existsOverlappingBooking(eq(tenantId), eq(staffId), eq(testStartAt), eq(testEndAt)))
                .thenReturn(false);

        List<StaffResponse> result = availabilityService.getEligibleStaff(serviceId, testStartAt);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(staffId);
        assertThat(result.get(0).name()).isEqualTo("Alex Green");
    }

    @Test
    @DisplayName("Should exclude staff when slot overlaps a scheduled break window")
    void shouldExcludeStaff_WhenSlotOverlapsBreak() {
        StaffAvailability workWindow = StaffAvailability.builder()
                .tenantId(tenantId)
                .staffId(staffId)
                .dayOfWeek(1)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .type(AvailabilityType.WORKING_HOURS)
                .build();

        StaffAvailability breakWindow = StaffAvailability.builder()
                .tenantId(tenantId)
                .staffId(staffId)
                .dayOfWeek(1)
                .startTime(LocalTime.of(10, 30))
                .endTime(LocalTime.of(11, 30))
                .type(AvailabilityType.BREAK)
                .build();

        when(serviceRepository.findByIdAndTenantId(serviceId, tenantId)).thenReturn(Optional.of(service));
        when(staffRepository.findActiveStaffByService(tenantId, serviceId)).thenReturn(List.of(staff));
        when(availabilityRepository.findByStaffAndDayAndType(eq(tenantId), eq(staffId), eq(1), eq(AvailabilityType.WORKING_HOURS)))
                .thenReturn(List.of(workWindow));
        when(availabilityRepository.findByStaffAndDayAndType(eq(tenantId), eq(staffId), eq(1), eq(AvailabilityType.BREAK)))
                .thenReturn(List.of(breakWindow));

        List<StaffResponse> result = availabilityService.getEligibleStaff(serviceId, testStartAt);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should exclude staff when slot extends past working hours")
    void shouldExcludeStaff_WhenSlotExceedsWorkingHours() {
        StaffAvailability workWindow = StaffAvailability.builder()
                .tenantId(tenantId)
                .staffId(staffId)
                .dayOfWeek(1)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 30)) // Ends at 10:30, but service requires 10:00 - 11:00
                .type(AvailabilityType.WORKING_HOURS)
                .build();

        when(serviceRepository.findByIdAndTenantId(serviceId, tenantId)).thenReturn(Optional.of(service));
        when(staffRepository.findActiveStaffByService(tenantId, serviceId)).thenReturn(List.of(staff));
        when(availabilityRepository.findByStaffAndDayAndType(eq(tenantId), eq(staffId), eq(1), eq(AvailabilityType.WORKING_HOURS)))
                .thenReturn(List.of(workWindow));

        List<StaffResponse> result = availabilityService.getEligibleStaff(serviceId, testStartAt);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should exclude staff when an overlapping confirmed booking exists")
    void shouldExcludeStaff_WhenBookingOverlaps() {
        StaffAvailability workWindow = StaffAvailability.builder()
                .tenantId(tenantId)
                .staffId(staffId)
                .dayOfWeek(1)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .type(AvailabilityType.WORKING_HOURS)
                .build();

        when(serviceRepository.findByIdAndTenantId(serviceId, tenantId)).thenReturn(Optional.of(service));
        when(staffRepository.findActiveStaffByService(tenantId, serviceId)).thenReturn(List.of(staff));
        when(availabilityRepository.findByStaffAndDayAndType(eq(tenantId), eq(staffId), eq(1), eq(AvailabilityType.WORKING_HOURS)))
                .thenReturn(List.of(workWindow));
        when(availabilityRepository.findByStaffAndDayAndType(eq(tenantId), eq(staffId), eq(1), eq(AvailabilityType.BREAK)))
                .thenReturn(List.of());
        when(bookingRepository.existsOverlappingBooking(eq(tenantId), eq(staffId), eq(testStartAt), eq(testEndAt)))
                .thenReturn(true);

        List<StaffResponse> result = availabilityService.getEligibleStaff(serviceId, testStartAt);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list immediately when service status is INACTIVE")
    void shouldReturnEmptyList_WhenServiceIsInactive() {
        service.setStatus(ServiceStatus.INACTIVE);
        when(serviceRepository.findByIdAndTenantId(serviceId, tenantId)).thenReturn(Optional.of(service));

        List<StaffResponse> result = availabilityService.getEligibleStaff(serviceId, testStartAt);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when service ID does not exist")
    void shouldThrowResourceNotFoundException_WhenServiceNotFound() {
        when(serviceRepository.findByIdAndTenantId(serviceId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> availabilityService.getEligibleStaff(serviceId, testStartAt))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Service not found");
    }
}