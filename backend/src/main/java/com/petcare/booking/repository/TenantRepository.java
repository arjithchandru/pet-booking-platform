package com.petcare.booking.repository;

import com.petcare.booking.domain.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {}