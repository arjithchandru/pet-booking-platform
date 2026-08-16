package com.petcare.booking.repository;

import com.petcare.booking.domain.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceRepository extends JpaRepository<Service, UUID> {

    List<Service> findAllByTenantId(UUID tenantId);

    List<Service> findByTenantId(UUID tenantId);

    Optional<Service> findByIdAndTenantId(UUID id, UUID tenantId);
}