package com.my.relink.controller.exchangeItem.dto.resp;

import com.my.relink.domain.item.donation.ItemQuality;
import com.my.relink.domain.item.exchange.repository.dto.FindExchangeItemListRepositoryDto;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.util.DateTimeFormatterUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FindAllExchangeItemListRespDto {
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
    private String createdAt;

    public FindAllExchangeItemListRespDto(FindExchangeItemListRepositoryDto dto) {
        this.exchangeItemId = dto.getExchangeItemId();
        this.exchangeItemName = dto.getExchangeItemName();
        this.tradeStatus = dto.getTradeStatus();
        this.itemQuality = dto.getItemQuality();
        this.desiredItem = dto.getDesiredItem();
        this.imageUrl = dto.getImageUrl();
        this.ownerId = dto.getOwnerId();
        this.ownerNickname = dto.getOwnerNickname();
        this.ownerTrustScore = dto.getOwnerTrustScore();
        this.description = dto.getDescription();
        this.categoryName = dto.getCategoryName();
        this.deposit = dto.getDeposit();
        this.createdAt = DateTimeFormatterUtil.format(dto.getCreatedAt());
    }
}
