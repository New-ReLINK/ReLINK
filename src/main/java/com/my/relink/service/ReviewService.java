package com.my.relink.service;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.review.dto.request.ReviewReqDto;
import com.my.relink.controller.review.dto.resp.ReviewDetailsRespDto;
import com.my.relink.controller.review.dto.resp.ReviewListRespDto;
import com.my.relink.controller.review.dto.resp.ReviewRespDto;
import com.my.relink.controller.trade.dto.response.ViewReviewRespDto;
import com.my.relink.domain.item.exchange.ExchangeItem;
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
import com.my.relink.util.DateTimeUtil;
import com.my.relink.util.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
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

    public ReviewRespDto postTradeReview(Long tradeId, ReviewReqDto reqDto, AuthUser authUser) {
        User currentUser = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));

        if(trade.getTradeStatus()!= TradeStatus.EXCHANGED){
            throw new BusinessException(ErrorCode.TRADE_ACCESS_DENIED);
        }

        User writer = currentUser;
        ExchangeItem exchangeItem = trade.getPartnerExchangeItem(writer.getId());

        Review review = Review.builder()
                .star(reqDto.getStar())
                .description(reqDto.getDescription())
                .writer(writer)
                .exchangeItem(exchangeItem)
                .build();

        reviewRepository.save(review);

        return new ReviewRespDto(review.getId());
    }
}
