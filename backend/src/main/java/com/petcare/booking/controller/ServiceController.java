package com.petcare.booking.controller;

import com.petcare.booking.dto.request.CreateServiceRequest;
import com.petcare.booking.dto.request.UpdateServiceRequest;
import com.petcare.booking.dto.response.ServiceResponse;
import com.petcare.booking.dto.response.StaffResponse;
import com.petcare.booking.service.AvailabilityService;
import com.petcare.booking.service.ServiceCatalogService;
import com.petcare.booking.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@Tag(name = "Services Management")
public class ServiceController {

    private final ServiceCatalogService serviceCatalogService;
    private final StaffService staffService;
    private final AvailabilityService availabilityService;

    @GetMapping
    @Operation(summary = "List all services for the tenant")
    public ResponseEntity<List<ServiceResponse>> getAllServices() {
        return ResponseEntity.ok(serviceCatalogService.getAllServices());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get service by ID")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceCatalogService.getServiceById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new service")
    public ResponseEntity<ServiceResponse> createService(@Valid @RequestBody CreateServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceCatalogService.createService(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing service")
    public ResponseEntity<ServiceResponse> updateService(@PathVariable UUID id, @Valid @RequestBody UpdateServiceRequest request) {
        return ResponseEntity.ok(serviceCatalogService.updateService(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate service")
    public ResponseEntity<Void> deactivateService(@PathVariable UUID id) {
        serviceCatalogService.deactivateService(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{serviceId}/staff/{staffId}")
    @Operation(summary = "Assign a qualified staff member to a service")
    public ResponseEntity<Void> assignStaff(@PathVariable UUID serviceId, @PathVariable UUID staffId) {
        staffService.assignStaffToService(serviceId, staffId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{serviceId}/staff/{staffId}")
    @Operation(summary = "Remove a staff assignment from a service")
    public ResponseEntity<Void> removeStaffAssignment(@PathVariable UUID serviceId, @PathVariable UUID staffId) {
        staffService.removeStaffFromService(serviceId, staffId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{serviceId}/available-staff")
    @Operation(summary = "Calculate eligible staff for a service at a specific start time")
    public ResponseEntity<List<StaffResponse>> getAvailableStaff(@PathVariable UUID serviceId, @RequestParam Instant startAt) {
        return ResponseEntity.ok(availabilityService.getEligibleStaff(serviceId, startAt));
    }
}