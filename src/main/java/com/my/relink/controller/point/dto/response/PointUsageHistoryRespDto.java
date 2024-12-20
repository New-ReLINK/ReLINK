package com.my.relink.controller.point.dto.response;

import com.my.relink.domain.point.pointHistory.repository.dto.PointUsageHistoryDto;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.util.DateTimeUtil;
import com.my.relink.util.page.PageResponse;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
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


    public PointUsageHistoryRespDto(PointUsageHistoryDto dto, DateTimeUtil dateTimeUtil) {
        this.tradeId = dto.getTradeId();
        this.partnerExchangeItemName = dto.getPartnerExchangeItemName();
        this.tradeStatus = dto.getTradeStatus().toString();
        this.depositAmount = dto.getDepositAmount();
        this.refundAmount = dto.getRefundAmount();
        this.depositDateTime = dateTimeUtil.getUsagePointHistoryFormattedTime(dto.getDepositDateTime());
        this.refundDateTime = dateTimeUtil.getUsagePointHistoryFormattedTime(dto.getRefundDateTime());
    }

}
