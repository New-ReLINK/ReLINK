package com.my.relink.domain.review.repository;

import com.my.relink.domain.review.repository.dto.ReviewDetailRepositoryDto;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface CustomReviewRepository {

    @Transactional(readOnly = true)
    Optional<ReviewDetailRepositoryDto> getReviewDetails(Long userId, Long reviewId);
}
