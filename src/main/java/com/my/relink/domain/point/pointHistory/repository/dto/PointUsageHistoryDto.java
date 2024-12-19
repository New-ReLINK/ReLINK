package com.my.relink.domain.point.pointHistory.repository.dto;

import com.my.relink.domain.trade.TradeStatus;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
public class PointUsageHistoryDto {

    private Long tradeId;
    private String partnerExchangeItemName;
    private TradeStatus tradeStatus;
    private Integer depositAmount;
    private Integer refundAmount;
    private LocalDateTime depositDateTime;
    private LocalDateTime refundDateTime;

    public PointUsageHistoryDto(Long tradeId, String partnerExchangeItemName, TradeStatus tradeStatus, Integer depositAmount, Integer refundAmount, LocalDateTime depositDateTime, LocalDateTime refundDateTime) {
        this.tradeId = tradeId;
        this.partnerExchangeItemName = partnerExchangeItemName;
        this.tradeStatus = tradeStatus;
        this.depositAmount = depositAmount;
        this.refundAmount = refundAmount;
        this.depositDateTime = depositDateTime;
        this.refundDateTime = refundDateTime;
    }
}
