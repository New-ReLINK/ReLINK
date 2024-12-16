package com.my.relink.controller.trade.dto.response;

import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ViewReviewRespDto {
    private Long tradeId;
    private String partnerExchangeItemImage;
    private String partnerExchangeItemName;
    private String partnerNickname;
    private String completedAt;

    public static ViewReviewRespDto from(Trade trade, String partnerExchangeItemImage, User partner, ExchangeItem partnerExchangeItem, String completedAt) {
        return new ViewReviewRespDto(
                trade.getId(),
                partnerExchangeItemImage,
                partnerExchangeItem.getName(),
                partner.getNickname(),
                completedAt
        );

    }
}
