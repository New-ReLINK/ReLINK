package com.my.relink.service;

import com.my.relink.controller.trade.dto.response.TradeInquiryDetailRespDto;
import com.my.relink.domain.item.exchange.ExchangeItemRepository;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeRepository;
import com.my.relink.domain.user.User;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradeService {

    private final TradeRepository tradeRepository;
    private final UserService userService;
    private final ImageService imageService;


    /**
     * [문의하기] -> 해당 채팅방의 거래 정보, 상품 정보, 상대 유저 정보 내리기
     * @param tradeId
     * @param user
     * @return
     */
    public TradeInquiryDetailRespDto retrieveTradeDetail (Long tradeId, User user) {
        Trade trade = tradeRepository.findByIdWithItemsAndUser(tradeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_NOT_FOUND));

        trade.validateAccess(user);

        String requestedItemImageUrl = imageService.getExchangeItemUrl(trade.getRequesterExchangeItem());
        User partner = trade.getPartner(user);
        int trustScoreOfPartner = userService.getTrustScore(partner);

        return new TradeInquiryDetailRespDto(trade, partner, trustScoreOfPartner, requestedItemImageUrl);
    }

}
