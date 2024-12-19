package com.my.relink.domain.review.repository;

import com.my.relink.config.JpaConfig;
import com.my.relink.config.TestConfig;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import com.my.relink.domain.item.donation.ItemQuality;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.review.Review;
import com.my.relink.domain.review.TradeReview;
import com.my.relink.domain.review.repository.dto.ReviewDetailRepositoryDto;
import com.my.relink.domain.review.repository.dto.ReviewWithExchangeItemRepositoryDto;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.user.Address;
import com.my.relink.domain.user.User;
import com.my.relink.util.DateTimeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({TestConfig.class, JpaConfig.class})
class CustomReviewRepositoryImplTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    CustomReviewRepositoryImpl customReviewRepository;


    @Test
    @DisplayName("리뷰 단건 조회 성공")
    void getReviewDetailTest() {
        // given
        User user = createUser("tester1");
        User requester = createUser("tester2");
        ExchangeItem ownerItem = createExchangeItme(user, "item1");
        ExchangeItem requestItem = createExchangeItme(requester, "item2");
        Trade trade = createTrade(user, ownerItem, requestItem);
        Review review = createReview(requestItem, user);

        em.persist(user);
        em.persist(requester);
        em.persist(ownerItem);
        em.persist(requestItem);
        em.persist(trade);
        em.persist(review);
        em.flush();
        em.clear();

        Image image = createImage(requestItem.getId());
        em.persist(image);
        em.flush();
        em.clear();

        // when
        ReviewDetailRepositoryDto respDto = customReviewRepository.getReviewDetails(user.getId(), review.getId()).get();

        // then
        assertThat(respDto).isNotNull();
        assertThat(respDto.getItemName()).isEqualTo(requestItem.getName());
        assertThat(respDto.getPartnerNickname()).isEqualTo(requester.getName());
        assertThat(respDto.getTradeId()).isEqualTo(trade.getId());
        assertThat(respDto.getTradeStatusList()).hasSize(2)
                .containsExactly(TradeReview.TIME_PUNCTUAL, TradeReview.QUICK_RESPONSE);
        assertThat(respDto.getImages()).isNotNull();
        assertThat(respDto.getImages()).hasSize(1).extracting("imageUrl")
                .containsExactly("test image Url");
    }

    private User createUser(String name) {
        return User.builder()
                .name(name)
                .email(name + "@example.com")
                .nickname(name + "test")
                .isDeleted(false)
                .address(new Address(12345, "test", "test"))
                .build();
    }

    private ExchangeItem createExchangeItme(User user, String itemName) {
        return ExchangeItem.builder()
                .user(user)
                .name(itemName)
                .description("test description")
                .tradeStatus(TradeStatus.EXCHANGED)
                .brand("brand")
                .itemQuality(ItemQuality.NEW)
                .deposit(10)
                .isDeleted(false)
                .build();
    }

    private Trade createTrade(User user, ExchangeItem owner, ExchangeItem requester) {
        return Trade.builder()
                .requester(user)
                .ownerExchangeItem(owner)
                .requesterExchangeItem(requester)
                .hasOwnerReceived(false)
                .hasRequesterReceived(false)
                .hasOwnerRequested(false)
                .hasRequesterRequested(false)
                .build();
    }

    private Review createReview(ExchangeItem exchangeItem, User user) {
        return Review.builder()
                .writer(user)
                .tradeReview(List.of(TradeReview.TIME_PUNCTUAL, TradeReview.QUICK_RESPONSE))
                .exchangeItem(exchangeItem)
                .star(BigDecimal.valueOf(4.5))
                .build();
    }

    private Image createImage(Long entityId) {
        return Image.builder()
                .entityId(entityId)
                .entityType(EntityType.EXCHANGE_ITEM)
                .imageUrl("test image Url")
                .build();
    }


    @Test
    @DisplayName("나에게 달린 후기 목록 페이지 정상 조회")
    void findMyReviewSuccessTest() {
        // given
        User user = User.builder()
                .nickname("testUser")
                .build();
        em.persist(user);

        ExchangeItem exchangeItem1 = ExchangeItem.builder()
                .name("item2")
                .isDeleted(false)
                .user(user)
                .build();
        em.persist(exchangeItem1);

        User reviewer = User.builder()
                .nickname("reviewer")
                .build();
        em.persist(reviewer);

        ExchangeItem exchangeItem2 = ExchangeItem.builder()
                .name("item2")
                .isDeleted(false)
                .user(user)
                .build();
        em.persist(exchangeItem2);


        Review review = Review.builder()
                .exchangeItem(exchangeItem1)
                .star(BigDecimal.valueOf(4))
                .description("test description")
                .writer(reviewer)
                .build();
        em.persist(review);

        em.flush();
        em.clear();

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<ReviewWithExchangeItemRepositoryDto> reviews =
                customReviewRepository.findMyReviewsWithExchangeItems(user.getId(), pageable);

        // then
        assertThat(reviews).isNotNull();
        assertThat(reviews.getTotalElements()).isEqualTo(1);
        ReviewWithExchangeItemRepositoryDto content = reviews.getContent().get(0);
        assertThat(content).isNotNull();
        assertThat(content.getDescription()).isEqualTo(review.getDescription());
        assertThat(content.getNickname()).isEqualTo(reviewer.getNickname());
        assertThat(content.getItemName()).isEqualTo(exchangeItem1.getName());
    }

    @Test
    @DisplayName("리뷰가 없는 경우 빈페이지 반환")
    void reviewNotFoundSuccessTest() {
        // given
        User user = User.builder()
                .nickname("test")
                .build();

        em.persist(user);
        em.flush();
        em.clear();

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<ReviewWithExchangeItemRepositoryDto> reviews = customReviewRepository.findMyReviewsWithExchangeItems(user.getId(), pageable);

        // then
        assertThat(reviews).isNotNull();
        assertThat(reviews.getTotalElements()).isEqualTo(0);
        assertThat(reviews.getContent()).isEmpty();
    }

}