package com.my.relink.controller.review.dto.resp;

import com.my.relink.domain.review.TradeReview;
import com.my.relink.domain.review.repository.dto.ReviewListRepositoryDto;
import com.my.relink.util.DateTimeFormatterUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewListRespDto {
    private Long reviewId;
    private String itemName;
    private BigDecimal star;
    private String description;
    private String createdAt;
    private List<TradeReview> tradeStatusList;

    public ReviewListRespDto(ReviewListRepositoryDto dto) {
        this.reviewId = dto.getReviewId();
        this.itemName = dto.getItemName();
        this.star = dto.getStar();
        this.description = dto.getDescription().length() > 60 ? dto.getDescription().substring(0, 60) : dto.getDescription();
        this.createdAt = DateTimeFormatterUtil.format(dto.getCreatedAt());
        this.tradeStatusList = dto.getTradeStatusList();
    }
}
