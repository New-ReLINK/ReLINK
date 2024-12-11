package com.my.relink.controller.like.dto.resp;

import com.my.relink.domain.like.repository.dto.LikeExchangeItemListRepositoryDto;
import com.my.relink.domain.trade.TradeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LikeExchangeItemListRespDto {
    private Long itemId;
    private String itemName;
    private int trustScore;
    private TradeStatus tradeStatus;
    private String desiredItem;
    private String ownerNickname;
    private String imageUrl;

    public LikeExchangeItemListRespDto(LikeExchangeItemListRepositoryDto dto) {
        this.itemId = dto.getItemId();
        this.itemName = dto.getItemName();
        this.trustScore = (int) (dto.getAvgStar() * 20);
        this.tradeStatus = dto.getTradeStatus();
        this.desiredItem = dto.getDesiredItem();
        this.ownerNickname = dto.getOwnerNickname();
        this.imageUrl = dto.getImageUrl();
    }
}
