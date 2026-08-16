package com.petcare.booking.repository;

import com.petcare.booking.domain.entity.StaffAvailability;
import com.petcare.booking.domain.enums.AvailabilityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StaffAvailabilityRepository extends JpaRepository<StaffAvailability, UUID> {

    @Query("""
        SELECT a FROM StaffAvailability a 
        WHERE a.tenantId = :tenantId 
          AND a.staffId = :staffId 
          AND a.dayOfWeek = :dayOfWeek 
          AND a.type = :type
    """)
    List<StaffAvailability> findByStaffAndDayAndType(
            @Param("tenantId") UUID tenantId,
            @Param("staffId") UUID staffId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("type") AvailabilityType type
    );

    List<StaffAvailability> findAllByTenantIdAndStaffId(UUID tenantId, UUID staffId);

    List<StaffAvailability> findByTenantIdAndStaffId(UUID tenantId, UUID staffId);
}