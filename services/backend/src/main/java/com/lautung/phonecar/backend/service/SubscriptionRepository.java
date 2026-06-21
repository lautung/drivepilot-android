package com.lautung.phonecar.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, UUID> {
    List<SubscriptionEntity> findAllByUserIdOrderByPlan(UUID userId);
    Optional<SubscriptionEntity> findByUserIdAndPlan(UUID userId, SubscriptionPlan plan);
}
