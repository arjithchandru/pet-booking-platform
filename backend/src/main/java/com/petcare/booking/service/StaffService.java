package com.petcare.booking.service;

import com.petcare.booking.domain.entity.Service;
import com.petcare.booking.domain.entity.Staff;
import com.petcare.booking.domain.entity.StaffAvailability;
import com.petcare.booking.domain.enums.StaffStatus;
import com.petcare.booking.dto.request.AvailabilityWindowRequest;
import com.petcare.booking.dto.request.CreateStaffRequest;
import com.petcare.booking.dto.request.UpdateStaffRequest;
import com.petcare.booking.dto.response.StaffAvailabilityResponse;
import com.petcare.booking.dto.response.StaffResponse;
import com.petcare.booking.exception.BadRequestException;
import com.petcare.booking.exception.ResourceNotFoundException;
import com.petcare.booking.repository.ServiceRepository;
import com.petcare.booking.repository.StaffAvailabilityRepository;
import com.petcare.booking.repository.StaffRepository;
import com.petcare.booking.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;
    private final ServiceRepository serviceRepository;
    private final StaffAvailabilityRepository availabilityRepository;

    @Transactional(readOnly = true)
    public List<StaffResponse> getAllStaff() {
        UUID tenantId = TenantContext.getTenantId();
        return staffRepository.findAllByTenantId(tenantId).stream()
                .map(StaffResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public StaffResponse getStaffById(UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        return staffRepository.findByIdAndTenantId(id, tenantId)
                .map(StaffResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found"));
    }

    @Transactional
    public StaffResponse createStaff(CreateStaffRequest req) {
        UUID tenantId = TenantContext.getTenantId();

        Set<Service> qualifiedServices = new HashSet<>();
        if (req.serviceIds() != null && !req.serviceIds().isEmpty()) {
            for (UUID sId : req.serviceIds()) {
                serviceRepository.findByIdAndTenantId(sId, tenantId).ifPresent(qualifiedServices::add);
            }
        }

        Staff staff = Staff.builder()
                .tenantId(tenantId)
                .name(req.name().trim())
                .email(req.email().trim().toLowerCase())
                .status(req.status() != null ? req.status() : StaffStatus.ACTIVE)
                .services(qualifiedServices)
                .build();

        Staff saved = staffRepository.save(staff);
        log.info("Created staff member [{}] for tenant [{}]", saved.getName(), tenantId);
        return StaffResponse.fromEntity(saved);
    }

    @Transactional
    public StaffResponse updateStaff(UUID id, UpdateStaffRequest req) {
        UUID tenantId = TenantContext.getTenantId();
        Staff staff = staffRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found"));

        staff.setName(req.name().trim());
        staff.setEmail(req.email().trim().toLowerCase());
        if (req.status() != null) {
            staff.setStatus(req.status());
        }

        if (req.serviceIds() != null) {
            Set<Service> updated = new HashSet<>();
            for (UUID sId : req.serviceIds()) {
                serviceRepository.findByIdAndTenantId(sId, tenantId).ifPresent(updated::add);
            }
            staff.setServices(updated);
        }

        Staff updated = staffRepository.save(staff);
        log.info("Updated staff member [{}] for tenant [{}]", updated.getId(), tenantId);
        return StaffResponse.fromEntity(updated);
    }

    @Transactional
    public void assignStaffToService(UUID serviceId, UUID staffId) {
        UUID tenantId = TenantContext.getTenantId();
        Staff staff = staffRepository.findByIdAndTenantId(staffId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
        Service service = serviceRepository.findByIdAndTenantId(serviceId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        staff.getServices().add(service);
        staffRepository.save(staff);
        log.info("Assigned service [{}] to staff [{}]", service.getName(), staff.getName());
    }

    @Transactional
    public void removeStaffFromService(UUID serviceId, UUID staffId) {
        UUID tenantId = TenantContext.getTenantId();
        Staff staff = staffRepository.findByIdAndTenantId(staffId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

        staff.getServices().removeIf(s -> s.getId().equals(serviceId));
        staffRepository.save(staff);
        log.info("Removed service [{}] from staff [{}]", serviceId, staff.getName());
    }

    @Transactional(readOnly = true)
    public List<StaffAvailabilityResponse> getStaffAvailability(UUID staffId) {
        UUID tenantId = TenantContext.getTenantId();
        staffRepository.findByIdAndTenantId(staffId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found"));

        return availabilityRepository.findAllByTenantIdAndStaffId(tenantId, staffId).stream()
                .map(StaffAvailabilityResponse::fromEntity)
                .toList();
    }

    @Transactional
    public StaffAvailabilityResponse addAvailabilityWindow(UUID staffId, AvailabilityWindowRequest req) {
        UUID tenantId = TenantContext.getTenantId();
        staffRepository.findByIdAndTenantId(staffId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found"));

        if (!req.startTime().isBefore(req.endTime())) {
            throw new BadRequestException("Start time must be before end time");
        }

        StaffAvailability window = StaffAvailability.builder()
                .tenantId(tenantId)
                .staffId(staffId)
                .dayOfWeek(req.dayOfWeek())
                .startTime(req.startTime())
                .endTime(req.endTime())
                .type(req.type())
                .build();

        return StaffAvailabilityResponse.fromEntity(availabilityRepository.save(window));
    }
}