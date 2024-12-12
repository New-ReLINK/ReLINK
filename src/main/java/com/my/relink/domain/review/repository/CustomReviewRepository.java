package com.my.relink.domain.review.repository;

import com.my.relink.domain.review.repository.dto.ReviewDetailRepositoryDto;
import com.my.relink.domain.review.repository.dto.ReviewListRepositoryDto;
import com.my.relink.domain.review.repository.dto.ReviewWithExchangeItemRepositoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface CustomReviewRepository {

    @Transactional(readOnly = true)
    Optional<ReviewDetailRepositoryDto> getReviewDetails(Long userId, Long reviewId);

    @Transactional(readOnly = true)
    Page<ReviewListRepositoryDto> findAllReviews(Long userId, Pageable pageable);

    @Transactional(readOnly = true)
    Page<ReviewWithExchangeItemRepositoryDto> findMyReviewsWithExchangeItems(Long userId, Pageable pageable);
}
