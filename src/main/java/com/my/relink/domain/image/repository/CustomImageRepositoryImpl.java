package com.my.relink.domain.image.repository;

import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import com.my.relink.domain.image.QImage;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomImageRepositoryImpl implements CustomImageRepository {

    private final JPAQueryFactory queryFactory;

    public List<Image> findFirstImages(EntityType entityType, List<Long> entityIds) {
        QImage image = QImage.image;
        return queryFactory.selectFrom(image)
                .where(image.entityType.eq(entityType)
                        .and(image.entityId.in(entityIds)))
                .orderBy(image.entityId.asc(), image.createdAt.asc())
                .fetch();
    }
}
