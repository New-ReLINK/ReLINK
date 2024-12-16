package com.my.relink.controller.review.dto.request;

import com.my.relink.domain.review.TradeReview;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ReviewReqDto {

    @Digits(integer = 1, fraction = 2)
    @NotNull(message = "별점은 소수점 1자리까지 허용됩니다.")
    private BigDecimal star;
    @NotNull(message = "거래에 대한 후기를 작성하세요.")
    private TradeReview tradeReview;
    private String description;

}
