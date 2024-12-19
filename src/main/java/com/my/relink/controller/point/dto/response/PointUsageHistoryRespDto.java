package com.my.relink.controller.point.dto.response;

import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.util.DateTimeUtil;
import com.my.relink.util.page.PageResponse;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@ToString
public class PointUsageHistoryRespDto {

    private Long tradeId;
    private String partnerExchangeItemName;
    private String tradeStatus;
    @Setter
    private Integer depositAmount;
    @Setter
    private Integer refundAmount;
    @Setter
    private String depositDateTime;
    @Setter
    private String refundDateTime;


    @Builder
    public PointUsageHistoryRespDto(Long tradeId, String partnerExchangeItemName, String tradeStatus, Integer depositAmount, Integer refundAmount, String depositDateTime, String refundDateTime) {
        this.tradeId = tradeId;
        this.partnerExchangeItemName = partnerExchangeItemName;
        this.tradeStatus = tradeStatus;
        this.depositAmount = depositAmount;
        this.refundAmount = refundAmount;
        this.depositDateTime = depositDateTime;
        this.refundDateTime = refundDateTime;
    }

}
