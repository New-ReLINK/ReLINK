package com.my.relink.domain.review.repository;

import com.my.relink.config.TestConfig;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.review.Review;
import com.my.relink.domain.review.TradeReview;
import com.my.relink.domain.review.repository.dto.ReviewListRepositoryDto;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.user.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
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
class CustomReviewRepositoryTest {

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    EntityManager em;

    private User user;

    @BeforeEach
    void init() {
        user = User.builder()
                .email("test@example.com")
                .name("test")
                .nickname("testNick")
                .isDeleted(false)
                .build();

        em.persist(user);

        ExchangeItem testItem = ExchangeItem.builder()
                .name("test item0")
                .tradeStatus(TradeStatus.EXCHANGED)
                .isDeleted(false)
                .build();

        em.persist(testItem);

        Review testReview = Review.builder()
                .writer(user)
                .star(BigDecimal.valueOf(3.5))
                .tradeReview(List.of(TradeReview.KIND_AND_MANNERED, TradeReview.QUICK_RESPONSE))
                .description("test description0")
                .exchangeItem(testItem)
                .build();

        em.persist(testReview);

        em.flush();
        em.clear();

        for (int i = 1; i <= 10; i++) {
            ExchangeItem item = ExchangeItem.builder()
                    .name("test item" + i)
                    .tradeStatus(TradeStatus.EXCHANGED)
                    .isDeleted(false)
                    .build();

            em.persist(item);

            Review review = Review.builder()
                    .writer(user)
                    .star(BigDecimal.valueOf(i % 5))
                    .tradeReview(List.of(TradeReview.TIME_PUNCTUAL, TradeReview.QUICK_RESPONSE))
                    .description("test description" + i)
                    .exchangeItem(item)
                    .build();

            em.persist(review);
        }

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("사용자의 리뷰 목록을 페이징하여 조회한다.")
    void reviewListSuccessTest() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<ReviewListRepositoryDto> allReviews = reviewRepository.findAllReviews(user.getId(), pageable);

        // then
        assertThat(allReviews).isNotNull();
        assertThat(allReviews.getContent()).hasSize(10);
        assertThat(allReviews.getTotalElements()).isEqualTo(11);

        ReviewListRepositoryDto firstReview = allReviews.getContent().get(0);

        assertThat(firstReview.getItemName()).isEqualTo("test item10");
        assertThat(firstReview.getStar()).isEqualTo(new BigDecimal("0.0"));
        assertThat(firstReview.getTradeStatusList()).hasSize(2);

        ReviewListRepositoryDto secondReview = allReviews.getContent().get(1);

        assertThat(secondReview.getItemName()).isEqualTo("test item9");
        assertThat(secondReview.getStar()).isEqualTo(new BigDecimal("4.0"));
        assertThat(secondReview.getTradeStatusList()).hasSize(2);
    }

    @Test
    @DisplayName("존재 하지 않는 사용자의 리뷰 목록을 조회하면 빈 리스트를 출력한다.")
    void userNotFoundEmptyListTest() {
        // given
        Long userId = 1000L;
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<ReviewListRepositoryDto> allReviews = reviewRepository.findAllReviews(userId, pageable);

        // then
        assertThat(allReviews.getContent()).isEmpty();
        assertThat(allReviews.getTotalElements()).isEqualTo(0);
        assertThat(allReviews.getTotalPages()).isEqualTo(0);
    }
}