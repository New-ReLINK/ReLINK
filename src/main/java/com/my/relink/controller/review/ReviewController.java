package com.my.relink.controller.review;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.review.dto.request.ReviewReqDto;
import com.my.relink.controller.review.dto.resp.ReviewDetailsRespDto;
import com.my.relink.controller.review.dto.resp.ReviewListRespDto;
import com.my.relink.controller.review.dto.resp.ReviewRespDto;
import com.my.relink.service.ReviewService;
import com.my.relink.util.api.ApiResult;
import com.my.relink.util.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/users/reviews/{reviewId}")
    public ResponseEntity<ApiResult<ReviewDetailsRespDto>> getReviewDetail(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long reviewId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResult.success(reviewService.findReviewDetailByUserAndReviewId(authUser.getId(), reviewId)));
    }

    @GetMapping("/users/reviews")
    public ResponseEntity<ApiResult<PageResponse<ReviewListRespDto>>> getReviewList(
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault(page = 0, size = 100) Pageable pageable
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResult.success(reviewService.findAllReviewByWriterUserId(authUser.getId(), pageable)));
    }

    @PostMapping("/trades/{tradeId}/review")
    public ResponseEntity<ApiResult<ReviewRespDto>> postTradeReview(
            @PathVariable(name = "tradeId") Long tradeId,
            @RequestBody ReviewReqDto reqDto,
            @AuthenticationPrincipal AuthUser authUser) {
        ReviewRespDto responseDto = reviewService.postTradeReview(tradeId, reqDto, authUser);
        return new ResponseEntity<>(ApiResult.success(responseDto), HttpStatus.CREATED);
    }

}
