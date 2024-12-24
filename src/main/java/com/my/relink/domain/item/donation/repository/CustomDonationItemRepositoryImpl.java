package com.my.relink.domain.item.donation.repository;

import com.my.relink.domain.category.QCategory;
import com.my.relink.domain.item.donation.DonationItem;
import com.my.relink.domain.item.donation.DonationStatus;
import com.my.relink.domain.item.donation.QDonationItem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class CustomDonationItemRepositoryImpl implements CustomDonationItemRepository{

    private final JPAQueryFactory queryFactory;
    QDonationItem donationItem = QDonationItem.donationItem;
    QCategory categoryEntity = QCategory.category;

    @Override
    public Page<DonationItem> findAllByFilters(String category, String search, Pageable pageable) {

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

    @Override
    public long countCompletedDonations() {
        Long count = queryFactory.select(donationItem.count())
                .from(donationItem)
                .where(donationItem.donationStatus.eq(DonationStatus.DONATION_COMPLETED))
                .fetchOne();
        return count != null ? count : 0L;

    }

    @Override
    public long countCompletedDonationsThisMonth() {
        Long count = queryFactory.select(donationItem.count())
                .from(donationItem)
                .where(
                        donationItem.donationStatus.eq(DonationStatus.DONATION_COMPLETED),
                        isCurrentMonth(donationItem.modifiedAt)
                )
                .fetchOne();

        return count != null ? count : 0L;
    }

    private BooleanExpression isCurrentMonth(DateTimePath<LocalDateTime> dateTimePath) {
        return Expressions.booleanTemplate(
                "FUNCTION('MONTH', {0}) = FUNCTION('MONTH', CURRENT_DATE) " +
                        "AND FUNCTION('YEAR', {0}) = FUNCTION('YEAR', CURRENT_DATE)",
                dateTimePath
        );
    }

    @Override
    public Optional<DonationItem> findByIdWithCategory(Long id) {
        DonationItem result = queryFactory.selectFrom(donationItem)
                .join(donationItem.category, categoryEntity).fetchJoin()
                .where(donationItem.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(result);
    }

}
