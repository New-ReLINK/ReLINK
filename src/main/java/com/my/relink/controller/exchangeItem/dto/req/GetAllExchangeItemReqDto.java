package com.my.relink.controller.exchangeItem.dto.req;

import com.my.relink.domain.trade.TradeStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GetAllExchangeItemReqDto {
    private String search;
    @Pattern(regexp = "asc|desc", flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "보증금 정렬 기준 값이 올바르지 않습니다.")
    private String deposit;
    private TradeStatus tradeStatus;
    private Long categoryId;
    @Builder.Default
    @Min(value = 1, message = "page는 1 이상으로 입력되어야 합니다.")
    private int page = 1;
    @Min(value = 1, message = "size는 1 이상으로 입력되어야합니다.")
    @Max(value = 100, message = "size는 100 이하로 입력되어야합니다.")
    @Builder.Default
    private int size = 100;
}
