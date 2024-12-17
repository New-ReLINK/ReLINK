package com.my.relink.domain.image.repository;

import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long>, CustomImageRepository {


    Optional<Image> findTopByEntityIdAndEntityTypeOrderByCreatedAtAsc(Long entityId, EntityType entityType);

    Optional<Image> findByEntityIdAndEntityType(Long entityId, EntityType entityType);

    Optional<Image> findByIdAndEntityIdAndEntityType(Long id, Long entityId, EntityType type);
}
