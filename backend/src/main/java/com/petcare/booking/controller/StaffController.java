package com.petcare.booking.controller;

import com.petcare.booking.dto.request.AvailabilityWindowRequest;
import com.petcare.booking.dto.request.CreateStaffRequest;
import com.petcare.booking.dto.request.UpdateStaffRequest;
import com.petcare.booking.dto.response.StaffAvailabilityResponse;
import com.petcare.booking.dto.response.StaffResponse;
import com.petcare.booking.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@Tag(name = "Staff Management & Availability")
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    @Operation(summary = "List all staff members for the tenant")
    public ResponseEntity<List<StaffResponse>> getAllStaff() {
        return ResponseEntity.ok(staffService.getAllStaff());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get staff member details by ID")
    public ResponseEntity<StaffResponse> getStaffById(@PathVariable UUID id) {
        return ResponseEntity.ok(staffService.getStaffById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new staff member")
    public ResponseEntity<StaffResponse> createStaff(@Valid @RequestBody CreateStaffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(staffService.createStaff(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update staff details and status")
    public ResponseEntity<StaffResponse> updateStaff(@PathVariable UUID id, @Valid @RequestBody UpdateStaffRequest request) {
        return ResponseEntity.ok(staffService.updateStaff(id, request));
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "Get weekly recurring working hours and breaks for staff")
    public ResponseEntity<List<StaffAvailabilityResponse>> getStaffAvailability(@PathVariable UUID id) {
        return ResponseEntity.ok(staffService.getStaffAvailability(id));
    }

    @PostMapping("/{id}/availability")
    @Operation(summary = "Add recurring working hour or break window")
    public ResponseEntity<StaffAvailabilityResponse> addAvailabilityWindow(@PathVariable UUID id, @Valid @RequestBody AvailabilityWindowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(staffService.addAvailabilityWindow(id, request));
    }
}