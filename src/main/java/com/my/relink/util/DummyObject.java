package com.my.relink.util;

import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.message.Message;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.user.Role;
import com.my.relink.domain.user.User;

import java.util.ArrayList;
import java.util.List;

public class DummyObject {

    protected User mockOwnerUser(){
        return User.builder()
                .nickname("maeda")
                .id(11L)
                .isDeleted(false)
                .build();
    }

    protected User mockRequesterUser(){
        return User.builder()
                .nickname("yushi")
                .id(12L)
                .isDeleted(false)
                .build();
    }

    protected Trade mockTrade(User owner, User requester){
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

    protected Trade mockTrade(User owner, User requester, boolean hasOwnerRequested, boolean hasRequesterRequested){
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

}
