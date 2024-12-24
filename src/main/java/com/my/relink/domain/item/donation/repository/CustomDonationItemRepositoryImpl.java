package com.my.relink.domain.item.donation.repository;

import com.my.relink.domain.category.QCategory;
import com.my.relink.domain.item.donation.DonationItem;
import com.my.relink.domain.item.donation.DonationStatus;
import com.my.relink.domain.item.donation.QDonationItem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomDonationItemRepositoryImpl implements CustomDonationItemRepository{

    private final JPAQueryFactory queryFactory;
    QDonationItem qDonationItem = QDonationItem.donationItem;

    @Override
    public Page<DonationItem> findAllByFilters(String category, String search, Pageable pageable) {
        QDonationItem donationItem = QDonationItem.donationItem;
        QCategory categoryEntity = QCategory.category;

        List<DonationItem> content = queryFactory.selectFrom(donationItem)
                .join(donationItem.category, categoryEntity)
                .where(
                        donationItem.donationStatus.eq(DonationStatus.DONATION_COMPLETED),
                        eqCategory(category, categoryEntity),
                        likeSearch(search, donationItem)
                )
                .orderBy(donationItem.modifiedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory.select(donationItem.count())
                .from(donationItem)
                .join(donationItem.category, categoryEntity)
                .where(
                        donationItem.donationStatus.eq(DonationStatus.DONATION_COMPLETED),
                        eqCategory(category, categoryEntity),
                        likeSearch(search, donationItem)
                )
                .fetchOne();

        return PageableExecutionUtils.getPage(content, pageable, () -> total != null ? total : 0);
    }

    private BooleanExpression eqCategory(String category, QCategory categoryEntity) {
        return category != null ? categoryEntity.id.eq(Long.parseLong(category)) : null;
    }

    private BooleanExpression likeSearch(String search, QDonationItem donationItem) {
        return search != null ? donationItem.name.containsIgnoreCase(search) : null;
    }

}
