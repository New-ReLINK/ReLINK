package com.my.relink.domain.review.repository.dto;

import com.my.relink.domain.review.TradeReview;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewListRepositoryDto {
    private Long reviewId;
    private String itemName;
    private BigDecimal star;
    private String description;
    private LocalDateTime createdAt;
    private List<TradeReview> tradeStatusList;

    public ReviewListRepositoryDto(ReviewListWithOutTradeStatusRepositoryDto dto, List<TradeReview> tradeReviews) {
        this.reviewId = dto.getReviewId();
        this.itemName = dto.getItemName();
        this.star = dto.getStar();
        this.description = dto.getDescription();
        this.createdAt = dto.getCreatedAt();
        this.tradeStatusList = tradeReviews;
    }
}
