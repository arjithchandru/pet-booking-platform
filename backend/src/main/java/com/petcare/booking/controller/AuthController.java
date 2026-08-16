package com.petcare.booking.controller;

import com.petcare.booking.dto.response.UserContextResponse;
import com.petcare.booking.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Auth & User Context")
public class AuthController {

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user and resolved tenant context")
    public ResponseEntity<UserContextResponse> getCurrentUserContext() {
        return ResponseEntity.ok(UserContextResponse.fromEntity(TenantContext.getCurrentUser()));
    }
}