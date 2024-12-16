package com.my.relink.domain.notification.repository;

import com.my.relink.config.JpaConfig;
import com.my.relink.config.TestConfig;
import com.my.relink.domain.BaseEntity;
import com.my.relink.domain.item.donation.DonationStatus;
import com.my.relink.domain.notification.Notification;
import com.my.relink.domain.notification.chat.ChatNotification;
import com.my.relink.domain.notification.chat.ChatStatus;
import com.my.relink.domain.notification.donation.DonationNotification;
import com.my.relink.domain.notification.exchange.ExchangeNotification;
import com.my.relink.domain.trade.TradeStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaConfig.class, TestConfig.class})
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;


    @Test
    @DisplayName("사용자 ID를 통해 전체 알림을 조회하면 생성일 기준 내림차순으로 정렬된 결과를 응답한다. ")
    void findByUserIdOrderByCreateAtDescSuccessTest() {
        // given
        Long userId = 1L;

        ChatNotification chatNotification = ChatNotification.builder()
                .userId(userId)
                .content("Chat Content")
                .requestUserNickname("tester")
                .exchangeItemName("test exchange")
                .chatStatus(ChatStatus.NEW_CHAT)
                .build();

        DonationNotification donationNotification = DonationNotification.builder()
                .userId(userId)
                .donationItemName("test donation")
                .donationStatus(DonationStatus.DONATION_COMPLETED)
                .build();

        ExchangeNotification exchangeNotification = ExchangeNotification.builder()
                .userId(userId)
                .exchangeItemName("test exchange")
                .requestUserNickname("tester")
                .tradeStatus(TradeStatus.EXCHANGED)
                .build();

        notificationRepository.saveAll(List.of(
                chatNotification,
                donationNotification,
                exchangeNotification
        ));

        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<Notification> result = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent())
                .extracting(BaseEntity::getCreatedAt)
                .isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 조회하면 빈 배열을 반환한다.")
    void findNotificationUserNotFoundSuccessTest() {
        // given
        Long userId = 999L;
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Notification> result = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("페이지 크기 만큼 알림 리스트를 반환한다.")
    void findNotificationWithPaginationSuccessTest() {
        // given
        Long userId = 1L;

        for (int i = 0; i < 10; i++) {
            ChatNotification chatNotification = ChatNotification.builder()
                    .userId(userId)
                    .content("Chat Content")
                    .requestUserNickname("tester" + i)
                    .exchangeItemName("test exchange" + i)
                    .chatStatus(ChatStatus.NEW_CHAT)
                    .build();

            DonationNotification donationNotification = DonationNotification.builder()
                    .userId(userId)
                    .donationItemName("test donation" + i)
                    .donationStatus(DonationStatus.DONATION_COMPLETED)
                    .build();

            ExchangeNotification exchangeNotification = ExchangeNotification.builder()
                    .userId(userId)
                    .exchangeItemName("test exchange" + i)
                    .requestUserNickname("tester" + i)
                    .tradeStatus(TradeStatus.EXCHANGED)
                    .build();
            notificationRepository.saveAll(List.of(chatNotification, donationNotification, exchangeNotification));
        }

        Pageable pageable = PageRequest.of(0, 10);

        // when

        Page<Notification> result = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(30);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(10);
    }
}