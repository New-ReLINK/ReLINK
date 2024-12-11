package com.my.relink.domain.like.repository.dto;

import com.my.relink.domain.trade.TradeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LikeExchangeItemListRepositoryDto {
    private Long itemId;
    private String itemName;
    private TradeStatus tradeStatus;
    private String desiredItem;
    private String ownerNickname;
    private String imageUrl;
    private Double avgStar;
}
