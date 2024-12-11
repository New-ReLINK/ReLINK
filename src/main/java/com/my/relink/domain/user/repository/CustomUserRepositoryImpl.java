package com.my.relink.domain.user.repository;

import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.item.donation.DonationStatus;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.user.repository.dto.UserInfoWithCountRepositoryDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.my.relink.domain.image.QImage.image;
import static com.my.relink.domain.item.donation.QDonationItem.donationItem;
import static com.my.relink.domain.item.exchange.QExchangeItem.exchangeItem;
import static com.my.relink.domain.point.QPoint.point;
import static com.my.relink.domain.review.QReview.review;
import static com.my.relink.domain.user.QUser.user;

@Repository
@RequiredArgsConstructor
public class CustomUserRepositoryImpl implements CustomUserRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<UserInfoWithCountRepositoryDto> findUserDetailInfo(Long userId) {
        JPAQuery<Long> donationItemCount = jpaQueryFactory.select(
                        donationItem.count().coalesce(0L)
                )
                .from(donationItem)
                .where(
                        donationItem.donationStatus.eq(DonationStatus.DONATION_COMPLETED),
                        donationItem.user.id.eq(userId)
                );

        JPAQuery<Long> exchangeItemCount = jpaQueryFactory.select(
                        exchangeItem.count().coalesce(0L)
                )
                .from(exchangeItem)
                .where(
                        exchangeItem.user.id.eq(userId),
                        exchangeItem.tradeStatus.eq(TradeStatus.EXCHANGED)
                );

        JPAQuery<String> imageUrl = jpaQueryFactory.select(image.imageUrl)
                .from(image)
                .where(
                        image.entityId.eq(user.id),
                        image.entityType.eq(EntityType.USER)
                );

        return Optional.ofNullable(jpaQueryFactory
                .select(Projections.constructor(UserInfoWithCountRepositoryDto.class,
                        donationItemCount,
                        exchangeItemCount,
                        user.name,
                        user.email,
                        point.amount,
                        imageUrl
                ))
                .from(user)
                .leftJoin(point).on(user.id.eq(point.user.id))
                .where(user.id.eq(userId))
                .fetchOne());
    }

    @Override
    public Double avgStar(Long userId) {
        return jpaQueryFactory.select(review.star.avg())
                .from(review)
                .join(review.exchangeItem, exchangeItem)
                .join(exchangeItem.user, user)
                .where(user.id.eq(userId))
                .fetchOne();
    }
}
