package com.my.relink.domain.image.repository;

import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long>, CustomImageRepository {
    Optional<Image> findByIdAndEntityIdAndEntityType(Long id, Long entityId, EntityType type);

    @Query("SELECT COUNT(i) FROM Image i WHERE i.entityId = :entityId AND i.entityType = :entityType")
    int countImages(@Param("entityId") Long entityId, @Param("entityType") EntityType entityType);
}
