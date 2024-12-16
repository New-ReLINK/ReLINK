package com.my.relink.controller.notification.dto.resp;

import com.my.relink.domain.notification.exchange.ExchangeNotification;
import com.my.relink.domain.trade.TradeStatus;
import lombok.Getter;

@Getter
public class ExchangeNotificationListRespDto extends NotificationListRespDto {
    private final TradeStatus status;
    private final String itemName;
    private final String requesterNickname;

    public ExchangeNotificationListRespDto(ExchangeNotification notification) {
        super(notification.getCreatedAt(), "EXCHANGE");
        this.status = notification.getTradeStatus();
        this.itemName = notification.getExchangeItemName();
        this.requesterNickname = notification.getRequestUserNickname();
    }
}
