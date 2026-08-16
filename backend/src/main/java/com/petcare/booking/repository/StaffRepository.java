package com.petcare.booking.repository;

import com.petcare.booking.domain.entity.Staff;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffRepository extends JpaRepository<Staff, UUID> {

    List<Staff> findAllByTenantId(UUID tenantId);

    List<Staff> findByTenantId(UUID tenantId);

    Optional<Staff> findByIdAndTenantId(UUID id, UUID tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Staff s WHERE s.id = :id AND s.tenantId = :tenantId")
    Optional<Staff> findByIdAndTenantIdWithLock(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query("""
        SELECT s FROM Staff s 
        JOIN s.services srv 
        WHERE s.tenantId = :tenantId 
          AND srv.id = :serviceId 
          AND s.status = 'ACTIVE'
    """)
    List<Staff> findActiveStaffByService(@Param("tenantId") UUID tenantId, @Param("serviceId") UUID serviceId);

    boolean existsByTenantIdAndEmail(UUID tenantId, String email);
}