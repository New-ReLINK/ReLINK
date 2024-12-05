package com.my.relink.domain.trade;

import com.my.relink.domain.BaseEntity;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.user.Address;
import com.my.relink.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Trade extends BaseEntity {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_exchange_item_id")
    private ExchangeItem ownerExchangeItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_exchange_item_id")
    private ExchangeItem requesterExchangeItem;

    @Enumerated(EnumType.STRING)
    private TradeStatus tradeStatus;

    @Column(length = 40)
    private String ownerTrackingNumber;

    @Column(length = 40)
    private String requesterTrackingNumber;

    @Embedded
    private Address ownerAddress;

    @Embedded
    private Address requesterAddress;

    @Column(nullable = false)
    private Boolean hasOwnerReceived = false;

    @Column(nullable = false)
    private Boolean hasRequesterReceived = false;

    @Column(nullable = false)
    private Boolean hasOwnerRequested = false;

    @Column(nullable = false)
    private Boolean hasRequesterRequested = false;
}
