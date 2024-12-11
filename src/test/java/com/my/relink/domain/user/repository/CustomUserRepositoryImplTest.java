package com.my.relink.domain.user.repository;

import com.my.relink.config.TestConfig;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import com.my.relink.domain.item.donation.DonationItem;
import com.my.relink.domain.item.donation.DonationStatus;
import com.my.relink.domain.item.donation.ItemQuality;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.point.Point;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.user.User;
import com.my.relink.domain.user.repository.dto.UserInfoWithCountRepositoryDto;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestConfig.class)
class CustomUserRepositoryImplTest {

    @Autowired
    private CustomUserRepositoryImpl userRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("유저 정보 상세 조회")
    void findUserProfile() {
        // given
        User user = User.builder()
                .name("test")
                .email("test@example.com")
                .isDeleted(false)
                .build();

        em.persist(user);

        Image image = Image.builder()
                .imageUrl("s3.org/test.jpg")
                .entityId(user.getId())
                .entityType(EntityType.USER)
                .build();

        em.persist(image);

        Point point = Point.builder()
                .user(user)
                .amount(0)
                .build();

        em.persist(point);

        for (int i = 0; i < 10; i++) {
            ExchangeItem exchangeItem = ExchangeItem.builder()
                    .itemQuality(ItemQuality.NEW)
                    .name("test exchange item" + i)
                    .description("test description" + i)
                    .tradeStatus(TradeStatus.EXCHANGED)
                    .user(user)
                    .isDeleted(false)
                    .build();

            em.persist(exchangeItem);

            DonationItem donationItem = DonationItem.builder()
                    .name("test donation item" + i)
                    .user(user)
                    .donationStatus(DonationStatus.DONATION_COMPLETED)
                    .build();

            em.persist(donationItem);
        }

        em.flush();
        em.clear();

        // when
        UserInfoWithCountRepositoryDto result = userRepository.findUserDetailInfo(user.getId()).get();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getProfileUrl()).isEqualTo("s3.org/test.jpg");
        assertThat(result.getDonationCount()).isEqualTo(10);
        assertThat(result.getExchangeCount()).isEqualTo(10);
        assertThat(result.getName()).isEqualTo("test");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getAmount()).isEqualTo(0);
    }

    @Test
    @DisplayName("유저 정보 상세 조회 시 기부 상품, 교환 상품이 없을 때 정상 조회")
    void findUserProfileWithOutCountSuccessTest() {
        // given
        User user = User.builder()
                .name("test user")
                .email("test@test.com")
                .isDeleted(false)
                .build();

        em.persist(user);

        Image image = Image.builder()
                .imageUrl("test/img.jpg")
                .entityId(user.getId())
                .entityType(EntityType.USER)
                .build();

        em.persist(image);
        em.flush();
        em.clear();

        // when
        UserInfoWithCountRepositoryDto result = userRepository.findUserDetailInfo(user.getId()).get();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("test user");
        assertThat(result.getProfileUrl()).isEqualTo("test/img.jpg");
        assertThat(result.getDonationCount()).isEqualTo(0);
        assertThat(result.getExchangeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("유저 프로픽 조회 시 프로필 이미지가 없을 때 정상 조회")
    void findUserProfileWithOutProfileImgUrlSuccessTest() {
        // given
        User testUser = User.builder()
                .name("user")
                .email("test@test.com")
                .isDeleted(false)
                .build();

        em.persist(testUser);

        // when
        UserInfoWithCountRepositoryDto result = userRepository.findUserDetailInfo(testUser.getId()).get();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getProfileUrl()).isNull();
    }

    @Test
    @DisplayName("유저 프로필 조회 시 포인트가 없을 때 정상 조회")
    void findUserProfileWithOutPointSuccessTest() {
        // given
        User testUser = User.builder()
                .name("test user")
                .email("test@test.com")
                .isDeleted(false)
                .build();

        em.persist(testUser);

        // when
        UserInfoWithCountRepositoryDto result = userRepository.findUserDetailInfo(testUser.getId()).get();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@test.com");
        assertThat(result.getName()).isEqualTo("test user");
    }
}