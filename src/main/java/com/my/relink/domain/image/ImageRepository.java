package com.my.relink.domain.image;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {

    Optional<Image> findTopByEntityIdAndEntityTypeOrderByCreatedAtAsc(Long entityId, EntityType entityType);
}
