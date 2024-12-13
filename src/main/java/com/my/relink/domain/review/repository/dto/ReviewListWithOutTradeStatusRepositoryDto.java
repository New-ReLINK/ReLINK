package com.my.relink.domain.review.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewListWithOutTradeStatusRepositoryDto {
    private Long reviewId;
    private String itemName;
    private BigDecimal star;
    private String description;
    private LocalDateTime createdAt;
}
