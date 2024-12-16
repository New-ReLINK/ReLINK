package com.my.relink.controller.trade.dto.response;

import com.my.relink.domain.image.Image;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ViewTradeCancelRespDto {
    private String partnerExchangeItemName;
    private String partnerNickname;
    private String tradeStartedAt;
    private String partnerExchangeItemImageUrl;

    public static ViewTradeCancelRespDto from(User partner, ExchangeItem partnerExchangeItem, String partnerExchangeItemImage, String tradeStartedAt){
        return new ViewTradeCancelRespDto(
                partnerExchangeItem.getName(),
                partner.getNickname(),
                tradeStartedAt,
                partnerExchangeItemImage
        );
    }

}
