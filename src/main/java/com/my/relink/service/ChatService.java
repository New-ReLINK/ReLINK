package com.my.relink.service;

import com.my.relink.controller.trade.dto.response.TradeResponse;
import com.my.relink.domain.item.exchange.ExchangeItemRepository;
import com.my.relink.domain.trade.TradeRepository;
import com.my.relink.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ExchangeItemRepository exchangeItemRepository;
    private final TradeRepository tradeRepository;

    public TradeResponse getChatRoomInfo(Long tradeId, User user) {


        return null;
    }

    /**
     * [문의하기] -> 해당 채팅방의 거래 정보, 상품 정보, 유저 정보 내리기
     */

}
