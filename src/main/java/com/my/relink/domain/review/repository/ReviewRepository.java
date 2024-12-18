package com.my.relink.domain.review.repository;

import com.my.relink.domain.review.Review;
import com.my.relink.domain.trade.TradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long>, CustomReviewRepository {

    @Query("select avg(r.star) " +
            "from Review r " +
            "where r.exchangeItem.user.id = :userId")
    Double getTotalStarAvg(@Param("userId") Long userId);

    @Query("select count(r) from Review r join ExchangeItem i on r.exchangeItem.id = i.id where i.user.id = :userId and i.tradeStatus = :status")
    long countByUserIdAndTradStatus(@Param("status") TradeStatus status, @Param("userId") Long userId);

    boolean existsByExchangeItemIdAndWriterId(Long exchangeItemId, Long writerId);
}
