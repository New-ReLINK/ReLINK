package com.my.relink.util;

import com.my.relink.domain.item.donation.ItemQuality;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.point.Point;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.user.User;

public class DummyObject {

    protected User mockOwnerUser() {
        return User.builder()
                .nickname("maeda")
                .id(11L)
                .isDeleted(false)
                .build();
    }

    protected User mockRequesterUser() {
        return User.builder()
                .nickname("yushi")
                .id(12L)
                .isDeleted(false)
                .build();
    }

    protected Trade mockTrade(User owner, User requester) {
        return Trade.builder()
                .requester(requester)
                .ownerExchangeItem(
                        ExchangeItem.builder()
                                .name("owner item")
                                .user(owner)
                                .isDeleted(false)
                                .deposit(10000)
                                .id(10L)
                                .build()
                )
                .requesterExchangeItem(
                        ExchangeItem.builder()
                                .name("requester item")
                                .user(requester)
                                .isDeleted(false)
                                .deposit(10000)
                                .id(13L)
                                .build()
                )
                .tradeStatus(TradeStatus.AVAILABLE)
                .hasOwnerRequested(false)
                .hasRequesterRequested(false)
                .hasOwnerReceived(false)
                .hasRequesterReceived(false)
                .build();
    }

    protected Trade mockTrade(User owner, User requester, boolean hasOwnerRequested, boolean hasRequesterRequested) {
        return Trade.builder()
                .requester(requester)
                .ownerExchangeItem(
                        ExchangeItem.builder()
                                .name("owner item")
                                .user(owner)
                                .isDeleted(false)
                                .deposit(10000)
                                .id(10L)
                                .build()
                )
                .requesterExchangeItem(
                        ExchangeItem.builder()
                                .name("requester item")
                                .user(requester)
                                .isDeleted(false)
                                .deposit(10000)
                                .id(13L)
                                .build()
                )
                .tradeStatus(TradeStatus.AVAILABLE)
                .hasOwnerRequested(hasOwnerRequested)
                .hasRequesterRequested(hasRequesterRequested)
                .hasOwnerReceived(false)
                .hasRequesterReceived(false)
                .build();
    }

    protected Trade mockTrade(User owner, User requester, boolean hasOwnerRequested, boolean hasRequesterRequested, boolean hasOwnerReceived, boolean hasRequesterReceived) {
        return Trade.builder()
                .requester(requester)
                .ownerExchangeItem(
                        ExchangeItem.builder()
                                .name("owner item")
                                .user(owner)
                                .isDeleted(false)
                                .deposit(10000)
                                .id(10L)
                                .itemQuality(ItemQuality.NEW)
                                .build()
                )
                .requesterExchangeItem(
                        ExchangeItem.builder()
                                .name("requester item")
                                .user(requester)
                                .isDeleted(false)
                                .deposit(10000)
                                .id(13L)
                                .itemQuality(ItemQuality.NEW)
                                .build()
                )
                .tradeStatus(TradeStatus.AVAILABLE)
                .hasOwnerRequested(hasOwnerRequested)
                .hasRequesterRequested(hasRequesterRequested)
                .hasOwnerReceived(hasOwnerReceived)
                .hasRequesterReceived(hasRequesterReceived)
                .build();
    }

    protected Trade mockTradeInExchange(User owner, User requester, boolean hasOwnerRequested, boolean hasRequesterRequested, boolean hasOwnerReceived, boolean hasRequesterReceived) {
        return Trade.builder()
                .requester(requester)
                .ownerExchangeItem(
                        ExchangeItem.builder()
                                .name("owner item")
                                .user(owner)
                                .isDeleted(false)
                                .deposit(10000)
                                .id(10L)
                                .itemQuality(ItemQuality.NEW)
                                .build()
                )
                .requesterExchangeItem(
                        ExchangeItem.builder()
                                .name("requester item")
                                .user(requester)
                                .isDeleted(false)
                                .deposit(10000)
                                .id(13L)
                                .itemQuality(ItemQuality.NEW)
                                .build()
                )
                .tradeStatus(TradeStatus.IN_EXCHANGE)
                .hasOwnerRequested(hasOwnerRequested)
                .hasRequesterRequested(hasRequesterRequested)
                .hasOwnerReceived(hasOwnerReceived)
                .hasRequesterReceived(hasRequesterReceived)
                .build();
    }

    protected Trade mockTradeExchanged(User owner, User requester, boolean hasOwnerRequested, boolean hasRequesterRequested, boolean hasOwnerReceived, boolean hasRequesterReceived) {
        return Trade.builder()
                .requester(requester)
                .ownerExchangeItem(
                        ExchangeItem.builder()
                                .name("owner item")
                                .user(owner)
                                .isDeleted(false)
                                .deposit(10000)
                                .id(10L)
                                .itemQuality(ItemQuality.NEW)
                                .build()
                )
                .requesterExchangeItem(
                        ExchangeItem.builder()
                                .name("requester item")
                                .user(requester)
                                .isDeleted(false)
                                .deposit(10000)
                                .id(13L)
                                .itemQuality(ItemQuality.NEW)
                                .build()
                )
                .tradeStatus(TradeStatus.EXCHANGED)
                .hasOwnerRequested(hasOwnerRequested)
                .hasRequesterRequested(hasRequesterRequested)
                .hasOwnerReceived(hasOwnerReceived)
                .hasRequesterReceived(hasRequesterReceived)
                .build();
    }

    protected Point mockRequesterPoint(Long id, Integer amount){
        return Point.builder()
                .id(id)
                .amount(amount)
                .user(mockRequesterUser())
                .build();
    }

    protected Point mockOwnerPoint(Long id, Integer amount){
        return Point.builder()
                .id(id)
                .amount(amount)
                .user(mockOwnerUser())
                .build();
    }

}
