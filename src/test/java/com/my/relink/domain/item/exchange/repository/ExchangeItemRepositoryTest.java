package com.my.relink.domain.item.exchange.repository;

import com.my.relink.config.JpaConfig;
import com.my.relink.config.TestConfig;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({TestConfig.class, JpaConfig.class})
class ExchangeItemRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    ExchangeItemRepository exchangeItemRepository;

    @Test
    @DisplayName("TradeStatus를 벌크 연산 할때 정상 동작한다.")
    void bulkTradeStatusSuccessTest() {
        // given
        User user = User.builder()
                .name("test")
                .password("test")
                .build();

        ExchangeItem item1 = ExchangeItem.builder()
                .name("item1")
                .user(user)
                .tradeStatus(TradeStatus.EXCHANGED)
                .isDeleted(false)
                .build();

        ExchangeItem item2 = ExchangeItem.builder()
                .name("item2")
                .user(user)
                .tradeStatus(TradeStatus.EXCHANGED)
                .isDeleted(false)
                .build();

        em.persist(user);
        em.persist(item1);
        em.persist(item2);

        em.flush();
        em.clear();

        // when
        exchangeItemRepository.updateTradeStatusToUnavailable(user.getId());

        // then
        Pageable pageable = PageRequest.of(0, 10);
        Page<ExchangeItem> result = exchangeItemRepository.findByUserId(user.getId(), pageable);

        List<ExchangeItem> content = result.getContent();
        assertThat(content).isNotNull();
        assertThat(content.get(0).getTradeStatus()).isEqualTo(TradeStatus.UNAVAILABLE);
        assertThat(content.get(1).getTradeStatus()).isEqualTo(TradeStatus.UNAVAILABLE);
    }
}