package com.my.relink.controller.exchangeItem.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ChoiceExchangeItemReqDto {
    @NotNull(message = "교환할 상품을 선택하여 주십시오.")
    private Long itemId;
}
