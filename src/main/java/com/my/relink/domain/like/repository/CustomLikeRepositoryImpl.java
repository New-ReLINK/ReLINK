package com.my.relink.domain.like.repository;

import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.like.QLike;
import com.my.relink.domain.like.repository.dto.LikeExchangeItemListRepositoryDto;
import com.my.relink.domain.user.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.my.relink.domain.image.QImage.image;
import static com.my.relink.domain.item.exchange.QExchangeItem.exchangeItem;
import static com.my.relink.domain.like.QLike.like;
import static com.my.relink.domain.review.QReview.review;

@Repository
@RequiredArgsConstructor
public class CustomLikeRepositoryImpl implements CustomLikeRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final QUser likedUser = new QUser("likedUser");
    private static final QLike like = QLike.like;

    @Override
    public Page<LikeExchangeItemListRepositoryDto> findUserLikeExchangeItem(Long userId, Pageable pageable) {
        JPQLQuery<String> firstImageSubQuery = JPAExpressions
                .select(image.imageUrl)
                .from(image)
                .where(
                        image.entityId.eq(exchangeItem.id),
                        image.entityType.eq(EntityType.EXCHANGE_ITEM)
                )
                .orderBy(image.createdAt.asc())
                .limit(1);

        JPQLQuery<Double> avgStarSubQuery = JPAExpressions
                .select(review.star.avg())
                .from(review)
                .where(review.exchangeItem.id.eq(exchangeItem.id));


        List<LikeExchangeItemListRepositoryDto> itemListDtos = jpaQueryFactory.select(
                        Projections.constructor(LikeExchangeItemListRepositoryDto.class,
                                exchangeItem.id,
                                exchangeItem.name,
                                exchangeItem.tradeStatus,
                                exchangeItem.desiredItem,
                                exchangeItem.user.nickname,
                                firstImageSubQuery,
                                avgStarSubQuery
                        )
                )
                .from(like)
                .join(like.user, likedUser)
                .join(like.exchangeItem, exchangeItem)
                .where(like.user.id.eq(userId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(like.createdAt.desc())
                .fetch();

        JPAQuery<Long> totalCount = jpaQueryFactory
                .select(exchangeItem.count())
                .from(like)
                .join(like.user, likedUser)
                .join(like.exchangeItem, exchangeItem)
                .where(like.user.id.eq(userId));

        return PageableExecutionUtils.getPage(itemListDtos, pageable, totalCount::fetchOne);
    }

    public Boolean existsLike(Long itemId, Long userId) {
        return jpaQueryFactory
                .selectOne()
                .from(like)
                .where(
                        like.exchangeItem.id.eq(itemId),
                        like.user.id.eq(userId)
                )
                .fetchFirst() != null;
    }
}
