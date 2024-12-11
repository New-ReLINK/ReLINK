package com.my.relink.controller.trade.dto.response;

import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.trade.Trade;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class ExchangeItemInfoDto {
    private String ownerItemName;
    private RequestedExchangeItemDto requestedItem; //신청자 아이템 정보


    public ExchangeItemInfoDto(Trade trade, String requestedItemImageUrl) {
        ownerItemName = trade.getOwnerExchangeItem().getName();
        requestedItem = new RequestedExchangeItemDto(trade.getRequesterExchangeItem(), requestedItemImageUrl);
    }

    @Getter
    @NoArgsConstructor
    public static class RequestedExchangeItemDto{
        public RequestedExchangeItemDto(ExchangeItem item, String requestedItemImageUrl) {
            this.imgUrl = requestedItemImageUrl;
            this.itemName = item.getName();
            this.itemId = item.getId();
        }

        private String imgUrl;
        private String itemName;
        private Long itemId;
    }
}
