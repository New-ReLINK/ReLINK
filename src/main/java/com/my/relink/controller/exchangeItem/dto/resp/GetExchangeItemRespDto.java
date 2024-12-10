package com.my.relink.controller.exchangeItem.dto.resp;

import com.my.relink.domain.trade.TradeStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class GetExchangeItemRespDto {
    // 공통
    Long exchangeItemId;
    String exchangeItemName;
    String imageUrl;
    TradeStatus tradeStatus;
    // 교환 전 AVAILABLE
    String desiredItem;
    // 교환 전, 중 (AVAILABLE, IN_EXCHANGE)
    String size;
    // 교환 중, 완료 (IN_EXCHANGE, EXCHANGED)
    String tradePartnerNickname;
    Long tradeId;
    // 교환 완료 EXCHANGED
    LocalDate completedDate;
}
