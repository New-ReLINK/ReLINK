package com.my.relink.domain.image.repository;

import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;

import java.util.List;
import java.util.Optional;

public interface CustomImageRepository {
    List<Image> findImages(EntityType entityType, List<Long> entityIds);

    List<Image> findFirstImages(EntityType entityType, List<Long> entityIds);

    Optional<Image> findFirstImage(Long entityId, EntityType entityType);

    void deleteImage(EntityType entityType, Long entityId);

    List<String> findImageUrls(EntityType entityType, Long itemId);
}
