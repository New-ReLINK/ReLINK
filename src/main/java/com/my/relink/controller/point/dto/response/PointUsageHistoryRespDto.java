package com.my.relink.controller.point.dto.response;

import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.util.DateTimeUtil;
import com.my.relink.util.page.PageResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class PointUsageHistoryRespDto {

    private Long tradeId;
    private String partnerExchangeItemName;
    private String tradeStatus;
    private Integer depositAmount;
    private Integer refundAmount;
    private String depositDateTime;
    private String refundDateTime;
    private LocalDateTime depositLocalDateTime;
    private LocalDateTime refundLocalDateTime;

    @Builder
    public PointUsageHistoryRespDto(Long tradeId, String partnerExchangeItemName, TradeStatus tradeStatus, Integer depositAmount, Integer refundAmount, LocalDateTime depositLocalDateTime, LocalDateTime refundLocalDateTime) {
        this.tradeId = tradeId;
        this.partnerExchangeItemName = partnerExchangeItemName;
        this.tradeStatus = tradeStatus.getMessage();
        this.depositAmount = depositAmount;
        this.refundAmount = refundAmount;
        this.depositLocalDateTime= depositLocalDateTime;
        this.refundLocalDateTime = refundLocalDateTime;
    }

    public void formatDateTime(DateTimeUtil dateTimeUtil){
        this.depositDateTime = dateTimeUtil.getUsagePointHistoryFormattedTime(depositLocalDateTime);
        this.refundDateTime = dateTimeUtil.getUsagePointHistoryFormattedTime(refundLocalDateTime);
    }
}
