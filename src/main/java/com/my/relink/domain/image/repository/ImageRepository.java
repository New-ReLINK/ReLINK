package com.my.relink.domain.image.repository;

import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long>, CustomImageRepository {


    Optional<Image> findTopByEntityIdAndEntityTypeOrderByCreatedAtAsc(Long entityId, EntityType entityType);

    Optional<Image> findByEntityIdAndEntityType(Long entityId, EntityType entityType);

    Optional<Image> findByIdAndEntityIdAndEntityType(Long id, Long entityId, EntityType type);

    void deleteByEntityTypeAndEntityId(EntityType entityType, Long entityId);

    @Query("SELECT i.imageUrl FROM Image i WHERE i.entityType = :entityType AND i.entityId = :itemId")
    List<String> findImageUrlsByItemId(@Param("entityType") EntityType entityType, @Param("itemId") Long itemId);
}
