package com.my.relink.controller.exchangeItem.dto.resp;

import com.my.relink.domain.trade.TradeStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Builder
@ToString
public class GetExchangeItemsByUserRespDto {
    private Long exchangeItemId;
    private String exchangeItemName;
    private String imageUrl;
    private TradeStatus tradeStatus;
    private String desiredItem;
    private String size;
    private String tradePartnerNickname;
    private Long tradeId;
    private LocalDate completedDate;
}
