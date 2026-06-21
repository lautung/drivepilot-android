package com.lautung.phonecar.backend.vehicle;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleStateRepository extends JpaRepository<VehicleStateEntity, UUID> {}
