package com.my.relink.domain.like.repository;

import com.my.relink.config.TestConfig;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import com.my.relink.domain.image.repository.ImageRepository;
import com.my.relink.domain.item.donation.ItemQuality;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.item.exchange.repository.ExchangeItemRepository;
import com.my.relink.domain.like.Like;
import com.my.relink.domain.like.repository.dto.LikeExchangeItemListRepositoryDto;
import com.my.relink.domain.review.Review;
import com.my.relink.domain.review.repository.ReviewRepository;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.user.Role;
import com.my.relink.domain.user.User;
import com.my.relink.domain.user.repository.UserRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestConfig.class)
class CustomLikeRepositoryImplTest {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExchangeItemRepository exchangeItemRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    TestEntityManager em;

    @Test
    @DisplayName("사용자가 좋아요한 교환 아이템 목록을 페이징하여 조회한다")
    void findUserLikeExchangeItem() {
        // given
        User user1 = userRepository.save(User.builder()
                .nickname("testUser1")
                .email("test1@example.com")
                .role(Role.USER)
                .password("password1234")
                .isDeleted(false)
                .name("test")
                .build());

        User user2 = userRepository.save(User.builder()
                .nickname("testUser2")
                .email("test2@example.com")
                .role(Role.USER)
                .password("password1234")
                .isDeleted(false)
                .name("test")
                .build());


        ExchangeItem item1 = exchangeItemRepository.save(ExchangeItem.builder()
                .name("item1")
                .tradeStatus(TradeStatus.AVAILABLE)
                .desiredItem("desired1")
                .isDeleted(false)
                .user(user2)
                .itemQuality(ItemQuality.NEW)
                .deposit(10)
                .description("test description1")
                .build());

        ExchangeItem item2 = exchangeItemRepository.save(ExchangeItem.builder()
                .name("item2")
                .tradeStatus(TradeStatus.AVAILABLE)
                .desiredItem("desired2")
                .isDeleted(false)
                .user(user2)
                .itemQuality(ItemQuality.NEW)
                .deposit(10)
                .description("test description1")
                .build());

        Image image1 = imageRepository.save(Image.builder()
                .imageUrl("url1")
                .entityId(item1.getId())
                .entityType(EntityType.EXCHANGE_ITEM)
                .build());

        Review review1 = reviewRepository.save(Review.builder()
                .exchangeItem(item1)
                .star(new BigDecimal("4.5"))
                .build());

        Like like1 = likeRepository.save(Like.builder()
                .user(user1)
                .exchangeItem(item1)
                .build());

        em.flush();

        Like like2 = likeRepository.save(Like.builder()
                .user(user1)
                .exchangeItem(item2)
                .build());

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<LikeExchangeItemListRepositoryDto> result = likeRepository.findUserLikeExchangeItem(user1.getId(), pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        LikeExchangeItemListRepositoryDto firstItem = result.getContent().get(0);

        assertThat(firstItem.getItemId()).isEqualTo(item1.getId());
        assertThat(firstItem.getItemName()).isEqualTo(item1.getName());
        assertThat(firstItem.getTradeStatus()).isEqualTo(TradeStatus.AVAILABLE);
        assertThat(firstItem.getDesiredItem()).isEqualTo(item1.getDesiredItem());
        assertThat(firstItem.getOwnerNickname()).isEqualTo(user2.getNickname());
        assertThat(firstItem.getImageUrl()).isEqualTo(image1.getImageUrl());
        assertThat(firstItem.getAvgStar()).isEqualTo(4.5);
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 좋아요 목록을 조회하면 빈 페이지를 반환한다")
    void findUserLikeExchangeItem_UserNotFound() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Long nonExistentUserId = 1000L;

        // when
        Page<LikeExchangeItemListRepositoryDto> result = likeRepository.findUserLikeExchangeItem(nonExistentUserId, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("페이징 처리가 정상적으로 동작한다")
    void findUserLikeExchangeItem_Pagination() {
        // given
        User user = userRepository.save(User.builder().nickname("testUser").isDeleted(false).build());

        for (int i = 0; i < 20; i++) {
            ExchangeItem item = exchangeItemRepository.save(ExchangeItem.builder()
                    .name("item" + i)
                    .tradeStatus(TradeStatus.AVAILABLE)
                    .desiredItem("desired" + i)
                    .isDeleted(false)
                    .user(user)
                    .build());

            likeRepository.save(Like.builder()
                    .user(user)
                    .exchangeItem(item)
                    .build());
        }

        Pageable firstPage = PageRequest.of(0, 5);
        Pageable secondPage = PageRequest.of(1, 5);

        // when
        Page<LikeExchangeItemListRepositoryDto> firstResult = likeRepository.findUserLikeExchangeItem(user.getId(), firstPage);
        Page<LikeExchangeItemListRepositoryDto> secondResult = likeRepository.findUserLikeExchangeItem(user.getId(), secondPage);

        // then
        assertThat(firstResult.getContent()).hasSize(5);
        assertThat(firstResult.getTotalElements()).isEqualTo(20);
        assertThat(firstResult.getTotalPages()).isEqualTo(4);
        assertThat(firstResult.hasNext()).isTrue();
        assertThat(secondResult.getContent()).hasSize(5);
        assertThat(secondResult.getNumber()).isEqualTo(1);
    }
}