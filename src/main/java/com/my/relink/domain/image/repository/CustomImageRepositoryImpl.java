package com.my.relink.domain.image.repository;

import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import com.my.relink.domain.image.QImage;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CustomImageRepositoryImpl implements CustomImageRepository {

    private final JPAQueryFactory queryFactory;
    private static final QImage image = QImage.image;

    @Override
    public List<Image> findImages(EntityType entityType, List<Long> entityIds) {
        return queryFactory.selectFrom(image)
                .where(image.entityType.eq(entityType)
                        .and(image.entityId.in(entityIds)))
                .orderBy(image.createdAt.asc())
                .fetch();
    }

    @Override
    public List<Image> findFirstImages(EntityType entityType, List<Long> entityIds) {
        return queryFactory.selectFrom(image)
                .where(image.entityType.eq(entityType)
                        .and(image.entityId.in(entityIds)))
                .orderBy(image.entityId.asc(), image.createdAt.asc())
                .fetch();
    }

    @Override
    public Optional<Image> findFirstImage(Long entityId, EntityType entityType) {
        Image result = queryFactory
                .selectFrom(image)
                .where(
                        image.entityId.eq(entityId),
                        image.entityType.eq(entityType)
                )
                .orderBy(image.createdAt.asc())
                .fetchFirst();
        return Optional.ofNullable(result);
    }

    @Override
    public void deleteImage(EntityType entityType, Long entityId) {
        queryFactory
                .delete(image)
                .where(
                        image.entityType.eq(entityType),
                        image.entityId.eq(entityId)
                )
                .execute();
    }

    public List<String> findImageUrls(EntityType entityType, Long entityId) {
        return queryFactory
                .select(image.imageUrl)
                .from(image)
                .where(
                        image.entityId.eq(entityId),
                        image.entityType.eq(entityType)
                )
                .fetch();
    }


}
