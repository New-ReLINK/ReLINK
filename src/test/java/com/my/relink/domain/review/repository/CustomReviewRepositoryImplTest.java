package com.my.relink.domain.review.repository;

import com.my.relink.config.TestConfig;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import com.my.relink.domain.item.donation.ItemQuality;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.review.Review;
import com.my.relink.domain.review.TradeReview;
import com.my.relink.domain.review.repository.dto.ReviewDetailRepositoryDto;
import com.my.relink.domain.review.repository.dto.ReviewDetailWithExchangeItemRepositoryDto;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.user.Address;
import com.my.relink.domain.user.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestConfig.class)
class CustomReviewRepositoryImplTest {

    @Autowired
    EntityManager em;

    @Autowired
    CustomReviewRepositoryImpl reviewRepository;


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
        ReviewDetailRepositoryDto respDto = reviewRepository.getReviewDetails(user.getId(), review.getId()).get();

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
    @DisplayName("사용자 페이지 조회 시 리뷰 페이지가 정상적으로 조회한다.")
    void findReviewListSuccessTest() {
        // given
        User user1 = User.builder()
                .nickname("user1")
                .isDeleted(false)
                .build();

        User user2 = User.builder()
                .nickname("user2")
                .isDeleted(false)
                .build();

        em.persist(user1);
        em.persist(user2);
        em.flush();


        ExchangeItem item1 = ExchangeItem.builder()
                .name("item1")
                .user(user1)
                .isDeleted(false)
                .build();

        ExchangeItem item2 = ExchangeItem.builder()
                .name("item2")
                .user(user1)
                .isDeleted(false)
                .build();

        ExchangeItem item3 = ExchangeItem.builder()
                .name("item3")
                .user(user2)
                .isDeleted(false)
                .build();

        em.persist(item1);
        em.persist(item2);
        em.persist(item3);
        em.flush();

        Review review1 = Review.builder()
                .star(new BigDecimal("4"))
                .description("Good")
                .writer(user2)
                .exchangeItem(item1)
                .tradeReview(List.of(TradeReview.TIME_PUNCTUAL))
                .build();
        Review review2 = Review.builder()
                .star(new BigDecimal("5"))
                .description("Excellent")
                .writer(user2)
                .exchangeItem(item2)
                .tradeReview(List.of(TradeReview.TIME_PUNCTUAL))
                .build();

        em.persist(review1);
        em.persist(review2);

        em.flush();
        em.clear();

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<ReviewDetailWithExchangeItemRepositoryDto> allReviews = reviewRepository.findReviewDetails(user1.getId(), pageable);

        // then
        assertThat(allReviews).isNotNull();
        assertThat(allReviews.getContent()).hasSize(2);
        assertThat(allReviews.getTotalElements()).isEqualTo(2);
        assertThat(allReviews.getTotalPages()).isEqualTo(1);

        ReviewDetailWithExchangeItemRepositoryDto detail1 = allReviews.getContent().get(0);
        assertThat(detail1.getStar().doubleValue()).isEqualTo(review2.getStar().doubleValue());
        assertThat(detail1.getNickName()).isEqualTo(user2.getNickname());
        assertThat(detail1.getDescription()).isEqualTo(review2.getDescription());

        ReviewDetailWithExchangeItemRepositoryDto detail2 = allReviews.getContent().get(1);
        assertThat(detail2.getStar().doubleValue()).isEqualTo(review1.getStar().doubleValue());
        assertThat(detail2.getNickName()).isEqualTo(user2.getNickname());
        assertThat(detail2.getDescription()).isEqualTo(review1.getDescription());
    }
}