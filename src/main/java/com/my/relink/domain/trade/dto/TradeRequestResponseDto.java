package com.my.relink.domain.trade.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class TradeRequestResponseDto {
    private final Long id;

    public TradeRequestResponseDto(Long id) {
        this.id = id;
    }
}
