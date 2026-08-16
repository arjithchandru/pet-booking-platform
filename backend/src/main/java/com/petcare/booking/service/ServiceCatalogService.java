package com.petcare.booking.service;

import com.petcare.booking.domain.entity.Service;
import com.petcare.booking.domain.enums.ServiceStatus;
import com.petcare.booking.dto.request.CreateServiceRequest;
import com.petcare.booking.dto.request.UpdateServiceRequest;
import com.petcare.booking.dto.response.ServiceResponse;
import com.petcare.booking.exception.ResourceNotFoundException;
import com.petcare.booking.repository.ServiceRepository;
import com.petcare.booking.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceCatalogService {

    private final ServiceRepository serviceRepository;

    @Transactional(readOnly = true)
    public List<ServiceResponse> getAllServices() {
        UUID tenantId = TenantContext.getTenantId();
        return serviceRepository.findByTenantId(tenantId).stream()
                .map(ServiceResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceResponse getServiceById(UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        return serviceRepository.findByIdAndTenantId(id, tenantId)
                .map(ServiceResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
    }

    @Transactional
    public ServiceResponse createService(CreateServiceRequest req) {
        UUID tenantId = TenantContext.getTenantId();
        Service service = Service.builder()
                .tenantId(tenantId)
                .name(req.name().trim())
                .description(req.description() != null ? req.description().trim() : null)
                .category(req.category().trim())
                .durationMinutes(req.durationMinutes())
                .price(req.price())
                .status(req.status() != null ? req.status() : ServiceStatus.ACTIVE)
                .build();

        Service saved = serviceRepository.save(service);
        log.info("Created service [{}] with duration {}m for tenant [{}]", saved.getName(), saved.getDurationMinutes(), tenantId);
        return ServiceResponse.fromEntity(saved);
    }

    @Transactional
    public ServiceResponse updateService(UUID id, UpdateServiceRequest req) {
        UUID tenantId = TenantContext.getTenantId();
        Service service = serviceRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        service.setName(req.name().trim());
        service.setDescription(req.description() != null ? req.description().trim() : null);
        service.setCategory(req.category().trim());
        service.setDurationMinutes(req.durationMinutes());
        service.setPrice(req.price());
        if (req.status() != null) {
            service.setStatus(req.status());
        }

        Service updated = serviceRepository.save(service);
        log.info("Updated service [{}] for tenant [{}]", updated.getId(), tenantId);
        return ServiceResponse.fromEntity(updated);
    }

    @Transactional
    public void deactivateService(UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        Service service = serviceRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        service.setStatus(ServiceStatus.INACTIVE);
        serviceRepository.save(service);
        log.info("Deactivated service [{}] for tenant [{}]", id, tenantId);
    }
}