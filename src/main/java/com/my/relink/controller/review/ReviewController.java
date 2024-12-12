package com.my.relink.controller.review;

import com.my.relink.config.security.AuthUser;
import com.my.relink.controller.review.dto.resp.ReviewDetailsRespDto;
import com.my.relink.controller.review.dto.resp.ReviewPageInfoRespDto;
import com.my.relink.controller.review.dto.resp.ReviewStatisticsRespDto;
import com.my.relink.service.ReviewService;
import com.my.relink.util.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ApiResult<ReviewPageInfoRespDto>> getReviewList(
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResult.success(reviewService.findAllReviewByWriterUserId(authUser.getId(), pageable)));
    }

    @GetMapping("/users/reliability")
    public ResponseEntity<ApiResult<ReviewStatisticsRespDto>> getTrustScore(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResult.success(reviewService.calculateUserStatistics(authUser.getId())));
    }

}
