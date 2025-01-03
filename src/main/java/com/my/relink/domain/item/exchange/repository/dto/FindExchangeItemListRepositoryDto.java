package com.my.relink.domain.item.exchange.repository.dto;

import com.my.relink.domain.item.donation.ItemQuality;
import com.my.relink.domain.trade.TradeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FindExchangeItemListRepositoryDto {
    private Long exchangeItemId;
    private String exchangeItemName;
    private TradeStatus tradeStatus;
    private ItemQuality itemQuality;
    private String desiredItem;
    private String imageUrl;
    private Long ownerId;
    private String ownerNickname;
    private Double ownerTrustScore;
    private String description;
    private String categoryName;
    private Integer deposit;
    private LocalDateTime createdAt;
}
