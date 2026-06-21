package com.lautung.phonecar.backend.content;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscoveryContentRepository extends JpaRepository<DiscoveryContentEntity, UUID> {
    Page<DiscoveryContentEntity> findAllByStatus(ContentStatus status, Pageable pageable);
    Page<DiscoveryContentEntity> findAllByCategory(ContentCategory category, Pageable pageable);
    Page<DiscoveryContentEntity> findAllByStatusAndCategory(ContentStatus status, ContentCategory category, Pageable pageable);
    boolean existsByMediaId(UUID mediaId);
}
