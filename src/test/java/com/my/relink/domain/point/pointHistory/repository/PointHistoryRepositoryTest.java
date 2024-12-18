package com.my.relink.domain.point.pointHistory.repository;

import com.my.relink.controller.point.dto.response.PointUsageHistoryRespDto;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.item.exchange.repository.ExchangeItemRepository;
import com.my.relink.domain.point.Point;
import com.my.relink.domain.point.pointHistory.PointHistory;
import com.my.relink.domain.point.pointHistory.PointTransactionType;
import com.my.relink.domain.point.repository.PointRepository;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.trade.repository.TradeRepository;
import com.my.relink.domain.user.User;
import com.my.relink.domain.user.repository.UserRepository;
import com.my.relink.service.UserService;
import com.my.relink.util.page.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.mysema.commons.lang.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
class PointHistoryRepositoryTest {

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private ExchangeItemRepository exchangeItemRepository;



    @Test
    @DisplayName("포인트 사용 내역 조회: 각각의 거래건에 대해 보증금 예치/환급 내역이 정상 조회되어야 한다")
    void findPointUsageHistories() {
        User requester = createAndSaveUser("requester");
        User owner = createAndSaveUser("owner");

        Point requesterPoint = createAndSavePoint(requester);

        ExchangeItem requesterItem = createAndSaveExchangeItem("아이템1", requester);
        ExchangeItem ownerItem = createAndSaveExchangeItem("아이템2", owner);

        Trade trade1 = createAndSaveTrade(requester, owner, requesterItem, ownerItem);
        createAndSavePointHistory(trade1, requesterPoint, 1000, PointTransactionType.DEPOSIT);
        createAndSavePointHistory(trade1, requesterPoint, 1000, PointTransactionType.RETURN);

        Trade trade2 = createAndSaveTrade(owner, requester, ownerItem, requesterItem);
        createAndSavePointHistory(trade2, requesterPoint, 2000, PointTransactionType.DEPOSIT);

        PageResponse<PointUsageHistoryRespDto> result = pointHistoryRepository.findPointUsageHistories(requester, 0, 10);

        List<PointUsageHistoryRespDto> content = result.getContent();
        for (PointUsageHistoryRespDto pointUsageHistoryRespDto : content) {
            System.out.println("pointUsageHistoryRespDto = " + pointUsageHistoryRespDto);
        }


        assertEquals(result.getContent().size(), 2);
        assertEquals(result.getPageInfo().getTotalCount(), 2);

        // 최신순 정렬이므로 trade2가 먼저나와야 함
        PointUsageHistoryRespDto trade2History = result.getContent().get(0);
        assertEquals(trade2History.getTradeId(), trade2.getId());
        assertEquals(trade2History.getPartnerExchangeItemName(), ownerItem.getName());
        assertEquals(trade2History.getDepositAmount(), -2000);
        assertNull(trade2History.getRefundAmount());

        PointUsageHistoryRespDto trade1History = result.getContent().get(1);
        assertEquals(trade1History.getTradeId(), trade1.getId());
        assertEquals(trade1History.getPartnerExchangeItemName(), ownerItem.getName());
        assertEquals(trade1History.getDepositAmount(), -1000);
        assertEquals(trade1History.getRefundAmount(), 1000);
    }


    private User createAndSaveUser(String name) {
        User user = User.builder()
                .name(name)
                .email(name + "@naver.com")
                .password("password")
                .build();
        return userRepository.save(user);
    }

    private Point createAndSavePoint(User user) {
        Point point = Point.builder()
                .user(user)
                .amount(10000)
                .build();
        return pointRepository.save(point);
    }

    private ExchangeItem createAndSaveExchangeItem(String name, User owner) {
        ExchangeItem item = ExchangeItem.builder()
                .name(name)
                .description("상품입니다")
                .user(owner)
                .isDeleted(false)
                .build();
        return exchangeItemRepository.save(item);
    }

    private Trade createAndSaveTrade(User requester, User owner, ExchangeItem requesterItem, ExchangeItem ownerItem) {
        Trade trade = Trade.builder()
                .requester(requester)
                .requesterExchangeItem(requesterItem)
                .ownerExchangeItem(ownerItem)
                .tradeStatus(TradeStatus.EXCHANGED)
                .hasRequesterRequested(true)
                .hasOwnerRequested(true)
                .hasOwnerReceived(false)
                .hasRequesterReceived(false)
                .build();
        return tradeRepository.save(trade);
    }

    private PointHistory createAndSavePointHistory(Trade trade, Point point, int amount, PointTransactionType type) {
        PointHistory history = PointHistory.builder()
                .trade(trade)
                .point(point)
                .amount(amount)
                .pointTransactionType(type)
                .build();
        return pointHistoryRepository.save(history);
    }

}