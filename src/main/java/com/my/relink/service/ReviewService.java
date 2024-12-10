package com.my.relink.service;

import com.my.relink.controller.review.dto.resp.ReviewDetailsRespDto;
import com.my.relink.domain.review.repository.ReviewRepository;
import com.my.relink.domain.review.repository.dto.ReviewDetailRepositoryDto;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewDetailsRespDto findReviewDetailByUserAndReviewId(Long userId, Long reviewId){
        ReviewDetailRepositoryDto repositoryDto = reviewRepository.getReviewDetails(userId, reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
        return new ReviewDetailsRespDto(repositoryDto);
    }
}
