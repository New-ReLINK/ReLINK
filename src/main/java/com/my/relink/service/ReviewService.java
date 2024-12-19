package com.my.relink.service;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.review.dto.request.ReviewReqDto;
import com.my.relink.controller.review.dto.resp.*;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.item.exchange.repository.ExchangeItemRepository;
import com.my.relink.domain.review.Review;
import com.my.relink.domain.review.repository.ReviewRepository;
import com.my.relink.domain.review.repository.dto.ReviewDetailRepositoryDto;
import com.my.relink.domain.review.repository.dto.ReviewListRepositoryDto;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.trade.repository.TradeRepository;
import com.my.relink.domain.user.User;
import com.my.relink.domain.user.repository.UserRepository;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import com.my.relink.util.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ExchangeItemRepository exchangeItemRepository;
    private final UserRepository userRepository;
    private final TradeRepository tradeRepository;

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

    public ReviewStatisticsRespDto calculateUserStatistics(Long userId) {
        Double starAvg = reviewRepository.getTotalStarAvg(userId);
        long totalTradeCount = exchangeItemRepository.countByTradeStatusAndUserId(TradeStatus.EXCHANGED, userId);
        long totalReviewCount = reviewRepository.countByUserIdAndTradStatus(TradeStatus.EXCHANGED, userId);

        return new ReviewStatisticsRespDto(starAvg, totalTradeCount, totalReviewCount);
    }

    public PageResponse<ReviewWithExchangeItemListRespDto> getReviewWithExchange(Long userId, Pageable pageable) {
        Page<ReviewWithExchangeItemListRespDto> reviews = reviewRepository.findMyReviewsWithExchangeItems(userId, pageable)
                .map(ReviewWithExchangeItemListRespDto::new);

        return PageResponse.of(reviews);
    }

    @Transactional
    public ReviewRespDto postTradeReview(Long tradeId, ReviewReqDto reqDto, AuthUser authUser) {
        User currentUser = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Trade trade = tradeRepository.findByIdWithExchangeItem(tradeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));

        if (trade.getTradeStatus() != TradeStatus.EXCHANGED) {
            throw new BusinessException(ErrorCode.TRADE_NOT_COMPLETE);
        }

        ExchangeItem exchangeItem = trade.getPartnerExchangeItem(currentUser.getId());
         //거래 상대방이 탈퇴했을 떄
        User partnerUser = trade.getPartner(currentUser.getId());
        if(partnerUser.isDeleted()){
            throw new BusinessException(ErrorCode.USER_SECESSION);
        }

        if(reviewRepository.existsByExchangeItemIdAndWriterId(exchangeItem.getId(), currentUser.getId())){
            throw new BusinessException(ErrorCode.REVIEW_FORBIDDEN);
        }

        Review review = Review.builder()
                .star(reqDto.getStar())
                .description(reqDto.getDescription())
                .writer(currentUser)
                .exchangeItem(exchangeItem)
                .build();

        reviewRepository.save(review);

        return new ReviewRespDto(review.getId());
    }
}
