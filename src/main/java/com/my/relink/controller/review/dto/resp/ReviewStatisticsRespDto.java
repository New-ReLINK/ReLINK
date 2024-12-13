package com.my.relink.controller.review.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatisticsRespDto {
    private int reliability;
    private Double avgStar;
    private long totalTradeCount;
    private long totalReviewCount;

    public ReviewStatisticsRespDto(Double avgStar, long totalTradeCount, long totalReviewCount) {
        this.reliability = avgStar != null ? (int) (avgStar * 20) : 0;
        this.avgStar = avgStar;
        this.totalTradeCount = totalTradeCount;
        this.totalReviewCount = totalReviewCount;
    }
}
