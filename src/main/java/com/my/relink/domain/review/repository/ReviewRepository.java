package com.my.relink.domain.review.repository;

import com.my.relink.domain.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long>, CustomReviewRepository {

    @Query("select avg(r.star) " +
            "from Review r " +
            "where r.exchangeItem.user.id = :userId")
    Double getTotalStarAvg(@Param("userId") Long userId);
}
