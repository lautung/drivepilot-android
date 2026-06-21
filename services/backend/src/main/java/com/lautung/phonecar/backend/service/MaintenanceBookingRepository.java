package com.lautung.phonecar.backend.service;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface MaintenanceBookingRepository extends JpaRepository<MaintenanceBookingEntity, UUID> {
    Page<MaintenanceBookingEntity> findAllByUserId(UUID userId, Pageable pageable);
}
