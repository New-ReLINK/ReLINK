package com.my.relink.domain.image.repository;

import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;

import java.util.List;

public interface CustomImageRepository {
    List<Image> findImages(EntityType entityType, List<Long> entityIds);

    List<Image> findFirstImages(EntityType entityType, List<Long> entityIds);
}
