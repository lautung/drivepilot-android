package com.lautung.phonecar.backend.service;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface VehicleReservationRepository extends JpaRepository<VehicleReservationEntity, UUID> {
    Page<VehicleReservationEntity> findAllByUserId(UUID userId, Pageable pageable);
}
