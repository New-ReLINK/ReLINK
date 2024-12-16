package com.my.relink.domain.notification.exchange;

import com.my.relink.domain.notification.Notification;
import com.my.relink.domain.trade.TradeStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@DiscriminatorValue(value = "EXCHANGE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExchangeNotification extends Notification {

    @Column(length = 30)
    private String exchangeItemName;

    @Column(length = 20)
    private String requestUserNickname;

    @Enumerated(EnumType.STRING)
    private TradeStatus tradeStatus;

    @Builder
    public ExchangeNotification(Long userId, String exchangeItemName, String requestUserNickname, TradeStatus tradeStatus) {
        super(userId);
        this.exchangeItemName = exchangeItemName;
        this.requestUserNickname = requestUserNickname;
        this.tradeStatus = tradeStatus;
    }
}