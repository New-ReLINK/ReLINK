package com.my.relink.service;

import com.my.relink.controller.review.dto.resp.ReviewDetailsRespDto;
import com.my.relink.controller.review.dto.resp.ReviewListRespDto;
import com.my.relink.controller.review.dto.resp.ReviewPageInfoRespDto;
import com.my.relink.controller.review.dto.resp.ReviewStatisticsRespDto;
import com.my.relink.domain.item.exchange.repository.ExchangeItemRepository;
import com.my.relink.domain.review.repository.ReviewRepository;
import com.my.relink.domain.review.repository.dto.ReviewDetailRepositoryDto;
import com.my.relink.domain.review.repository.dto.ReviewListRepositoryDto;
import com.my.relink.domain.trade.TradeStatus;
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
    private final ExchangeItemRepository exchangeItemRepository;

    public ReviewDetailsRespDto findReviewDetailByUserAndReviewId(Long userId, Long reviewId) {
        ReviewDetailRepositoryDto repositoryDto = reviewRepository.getReviewDetails(userId, reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
        return new ReviewDetailsRespDto(repositoryDto);
    }

    public ReviewPageInfoRespDto findAllReviewByWriterUserId(Long userId, Pageable pageable) {
        Page<ReviewListRepositoryDto> allReviews = reviewRepository.findAllReviews(userId, pageable);
        long totalReviewCount = allReviews.getTotalElements();
        Page<ReviewListRespDto> respDtos = allReviews.map(ReviewListRespDto::new);
        return new ReviewPageInfoRespDto(totalReviewCount, PageResponse.of(respDtos));
    }

    public ReviewStatisticsRespDto calculateUserStatistics(Long userId) {
        Double starAvg = reviewRepository.getTotalStarAvg(userId);
        long totalTradeCount = exchangeItemRepository.countByTradeStatusAndUserId(TradeStatus.EXCHANGED, userId);
        long totalReviewCount = reviewRepository.countByUserIdAndTradStatus(TradeStatus.EXCHANGED, userId);

        return new ReviewStatisticsRespDto(starAvg, totalTradeCount, totalReviewCount);
    }
}
