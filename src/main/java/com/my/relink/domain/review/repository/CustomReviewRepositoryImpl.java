package com.my.relink.domain.review.repository;

import com.my.relink.controller.exchange.dto.resp.ExchangeItemImageListRespDto;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.review.Review;
import com.my.relink.domain.review.TradeReview;
import com.my.relink.domain.review.repository.dto.ReviewDetailRepositoryDto;
import com.my.relink.domain.review.repository.dto.ReviewDetailWithOutTradeReview;
import com.my.relink.domain.review.repository.dto.ReviewListRepositoryDto;
import com.my.relink.domain.review.repository.dto.ReviewListWithOutTradeStatusRepositoryDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.my.relink.domain.image.QImage.image;
import static com.my.relink.domain.item.exchange.QExchangeItem.exchangeItem;
import static com.my.relink.domain.review.QReview.review;
import static com.my.relink.domain.trade.QTrade.trade;
import static com.my.relink.domain.user.QUser.user;

@Repository
@RequiredArgsConstructor
public class CustomReviewRepositoryImpl implements CustomReviewRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<ReviewDetailRepositoryDto> getReviewDetails(Long userId, Long reviewId) {
        Optional<ReviewDetailWithOutTradeReview> reviewOptional = Optional.ofNullable(jpaQueryFactory.select(
                        Projections.constructor(ReviewDetailWithOutTradeReview.class,
                                trade.id,
                                exchangeItem.id,
                                exchangeItem.name,
                                exchangeItem.user.name,
                                review.description,
                                review.star,
                                review.createdAt
                        )
                )
                .from(review)
                .where(
                        review.writer.id.eq(userId),
                        review.id.eq(reviewId)
                )
                .leftJoin(review.exchangeItem, exchangeItem)
                .leftJoin(exchangeItem.user, user)
                .leftJoin(trade).on(
                        trade.ownerExchangeItem.eq(exchangeItem)
                                .or(trade.requesterExchangeItem.eq(exchangeItem))
                )
                .fetchOne());

        return reviewOptional.map(review -> {
            List<ExchangeItemImageListRespDto> imageList = getExchangeItemImageList(reviewOptional.get().getExchangeItemId());
            List<TradeReview> tradeReviews = getTradeReviews(reviewId);
            return new ReviewDetailRepositoryDto(reviewOptional.get(), tradeReviews, imageList);
        });
    }

    private List<TradeReview> getTradeReviews(Long reviewId) {
        return Optional.ofNullable(jpaQueryFactory.selectFrom(review)
                        .where(review.id.eq(reviewId))
                        .fetchOne())
                .map(Review::getTradeReview)
                .orElse(Collections.emptyList());
    }

    private List<ExchangeItemImageListRespDto> getExchangeItemImageList(Long exchangeId) {
        return jpaQueryFactory.select(Projections.constructor(
                                ExchangeItemImageListRespDto.class,
                                image.imageUrl
                        )
                )
                .from(image)
                .where(image.entityId.eq(exchangeId)
                        .and(image.entityType.eq(EntityType.EXCHANGE_ITEM)))
                .fetch();
    }

    @Override
    public Page<ReviewListRepositoryDto> findAllReviews(Long userId, Pageable pageable) {
        List<ReviewListWithOutTradeStatusRepositoryDto> reviewList = jpaQueryFactory.select(
                        Projections.constructor(ReviewListWithOutTradeStatusRepositoryDto.class,
                                review.id,
                                exchangeItem.name,
                                review.star,
                                review.description,
                                review.createdAt
                        )
                )
                .from(review)
                .leftJoin(review.exchangeItem, exchangeItem)
                .where(review.writer.id.eq(userId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(review.createdAt.desc())
                .fetch();

        JPAQuery<Long> totalCount = jpaQueryFactory.select(review.count())
                .from(review)
                .where(review.writer.id.eq(userId));

        List<Long> reviewIdList = reviewList.stream().map(ReviewListWithOutTradeStatusRepositoryDto::getReviewId)
                .toList();

        Map<Long, List<TradeReview>> reviewIdWithTradeReviews = getTradeReviews(reviewIdList);

        List<ReviewListRepositoryDto> content = reviewList.stream().map(e ->
                new ReviewListRepositoryDto(e, reviewIdWithTradeReviews.getOrDefault(e.getReviewId(), new ArrayList<>()))
        ).toList();

        return PageableExecutionUtils.getPage(content, pageable, totalCount::fetchOne);
    }

    private Map<Long, List<TradeReview>> getTradeReviews(List<Long> reviewIdList) {
        List<Review> tradeReview = jpaQueryFactory.selectFrom(review)
                .where(review.id.in(reviewIdList))
                .orderBy(review.createdAt.desc())
                .fetch();

        return tradeReview.stream()
                .collect(Collectors.toMap(
                        Review::getId,
                        Review::getTradeReview
                ));
    }
}
