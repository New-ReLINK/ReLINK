package com.my.relink.service;

import com.my.relink.controller.review.dto.resp.ReviewDetailsRespDto;
import com.my.relink.controller.review.dto.resp.ReviewListRespDto;
import com.my.relink.domain.review.repository.ReviewRepository;
import com.my.relink.domain.review.repository.dto.ReviewDetailRepositoryDto;
import com.my.relink.domain.review.repository.dto.ReviewListRepositoryDto;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import com.my.relink.util.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewDetailsRespDto findReviewDetailByUserAndReviewId(Long userId, Long reviewId) {
        ReviewDetailRepositoryDto repositoryDto = reviewRepository.getReviewDetails(userId, reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
        return new ReviewDetailsRespDto(repositoryDto);
    }

    public PageResponse<ReviewListRespDto> findAllReviewByWriterUserId(Long userId, Pageable pageable) {
        Page<ReviewListRepositoryDto> allReviews = reviewRepository.findAllReviews(userId, pageable);
        Page<ReviewListRespDto> respDtos = allReviews.map(ReviewListRespDto::new);
        return PageResponse.of(respDtos);
    }
}
