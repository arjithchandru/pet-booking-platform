package com.petcare.booking.isolation;

import com.petcare.booking.domain.entity.Tenant;
import com.petcare.booking.dto.response.ServiceResponse;
import com.petcare.booking.exception.ResourceNotFoundException;
import com.petcare.booking.repository.TenantRepository;
import com.petcare.booking.security.TenantContext;
import com.petcare.booking.service.ServiceCatalogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class TenantIsolationTest {

    @Autowired
    private ServiceCatalogService serviceCatalogService;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant happyPaws;
    private Tenant pawsPlay;

    @BeforeEach
    void setUp() {
        happyPaws = tenantRepository.findById(UUID.fromString("11111111-1111-1111-1111-111111111111")).orElseThrow();
        pawsPlay = tenantRepository.findById(UUID.fromString("22222222-2222-2222-2222-222222222222")).orElseThrow();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Tenant A cannot access Tenant B services by ID")
    void tenantCannotAccessOtherTenantResource() {
        // Set context to Tenant A (Happy Paws)
        TenantContext.setCurrentTenant(happyPaws);

        UUID tenantBServiceId = UUID.fromString("22222222-2222-0000-0000-000000000001"); // Dog Training

        assertThrows(ResourceNotFoundException.class, () -> {
            serviceCatalogService.getServiceById(tenantBServiceId);
        });
    }

    @Test
    @DisplayName("Tenant service list only returns resources belonging to the authenticated tenant")
    void listOnlyReturnsTenantSpecificData() {
        TenantContext.setCurrentTenant(happyPaws);
        List<ServiceResponse> happyPawsServices = serviceCatalogService.getAllServices();
        assertThat(happyPawsServices).allMatch(s -> !s.name().equals("Dog Training"));

        TenantContext.setCurrentTenant(pawsPlay);
        List<ServiceResponse> pawsPlayServices = serviceCatalogService.getAllServices();
        assertThat(pawsPlayServices).anyMatch(s -> s.name().equals("Dog Training"));
    }
}