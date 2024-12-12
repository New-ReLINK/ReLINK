package com.my.relink.controller.exchangeItem.dto.resp;

import com.my.relink.domain.trade.TradeStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Builder
@ToString
public class GetExchangeItemRespDto {
    // 공통
    private Long exchangeItemId;
    private String exchangeItemName;
    private String imageUrl;
    private TradeStatus tradeStatus;
    // 교환 전 AVAILABLE
    private String desiredItem;
    // 교환 전, 중 (AVAILABLE, IN_EXCHANGE)
    private String size;
    // 교환 중, 완료 (IN_EXCHANGE, EXCHANGED)
    private String tradePartnerNickname;
    private Long tradeId;
    // 교환 완료 EXCHANGED
    private LocalDate completedDate;
}
