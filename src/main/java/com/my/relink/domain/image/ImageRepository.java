package com.my.relink.domain.image;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {


    Optional<Image> findTopByEntityIdAndEntityTypeOrderByCreatedAtAsc(Long entityId, EntityType entityType);

    Optional<Image> findByEntityIdAndEntityType(Long entityId, EntityType entityType);


    @Query("SELECT i FROM Image i WHERE i.entityType = :entityType AND i.entityId IN :entityIds ORDER BY i.createdAt ASC")
    List<Image> findFirstImagesByEntityTypeAndEntityIds(@Param("entityType") EntityType entityType, @Param("entityIds") List<Long> entityIds);

}
