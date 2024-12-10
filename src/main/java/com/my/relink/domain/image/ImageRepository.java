package com.my.relink.domain.image;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {


    Optional<Image> findTopByEntityIdAndEntityTypeOrderByCreatedAtAsc(Long entityId, EntityType entityType);

    Optional<Image> findByEntityIdAndEntityType(Long entityId, EntityType entityType);


    @Query("SELECT i.imageUrl FROM Image i WHERE i.entityType = :entityType AND i.entityId = :entityId ORDER BY i.createdAt ASC")
    Optional<String> findFirstImageUrlByEntityTypeAndEntityIdOrderByCreatedAtAsc(
            @Param("entityType") EntityType entityType,
            @Param("entityId") Long entityId
    );
}
