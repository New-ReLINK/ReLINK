package com.my.relink.domain.review.repository;

import com.my.relink.controller.exchange.dto.resp.ExchangeItemImageListRespDto;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.review.TradeReview;
import com.my.relink.domain.review.repository.dto.ReviewDetailRepositoryDto;
import com.my.relink.domain.review.repository.dto.ReviewDetailWithOutTradeReview;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
    private final EntityManager em;

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

        if (reviewOptional.isEmpty()) {
            return Optional.empty();
        }

        List<ExchangeItemImageListRespDto> imageList = getExchangeItemImageList(reviewOptional.get().getExchangeItemId());
        List<TradeReview> tradeReviews = getTradeReviews(reviewId);
        return Optional.of(new ReviewDetailRepositoryDto(reviewOptional.get(), tradeReviews, imageList));
    }

    private List<TradeReview> getTradeReviews(Long reviewId) {
        return em.createQuery("select tr from Review r join r.tradeReview tr where r.id = :reviewId", TradeReview.class)
                .setParameter("reviewId", reviewId)
                .getResultList();
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
}
