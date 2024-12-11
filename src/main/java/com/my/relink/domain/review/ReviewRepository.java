package com.my.relink.domain.review;

import com.my.relink.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("select avg(r.star) " +
            "from Review r " +
            "where r.exchangeItem.user = :user")
    Double getTotalStarAvg(@Param("user") User user);
}
