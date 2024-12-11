package com.my.relink.domain.review.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDetailWithOutTradeReview {

    private Long tradeId;
    private Long exchangeItemId;
    private String itemName;
    private String partnerNickname;
    private String description;
    private BigDecimal star;
    private LocalDateTime completedDate;
}
