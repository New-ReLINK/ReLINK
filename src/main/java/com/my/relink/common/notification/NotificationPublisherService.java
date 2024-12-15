package com.my.relink.common.notification;

import com.my.relink.domain.item.donation.DonationStatus;
import com.my.relink.domain.notification.chat.ChatNotification;
import com.my.relink.domain.notification.chat.ChatStatus;
import com.my.relink.domain.notification.chat.repository.ChatNotificationRepository;
import com.my.relink.domain.notification.donation.DonationNotification;
import com.my.relink.domain.notification.donation.repository.DonationNotificationRepository;
import com.my.relink.domain.notification.exchange.ExchangeNotification;
import com.my.relink.domain.notification.exchange.repository.ExchangeNotificationRepository;
import com.my.relink.domain.trade.TradeStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationPublisherService {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ChatNotificationRepository chatNotificationRepository;
    private final DonationNotificationRepository donationNotificationRepository;
    private final ExchangeNotificationRepository exchangeNotificationRepository;

    @Transactional
    public void createExchangeNotification(Long userId, String exchangeItemName, String requesterNickname, TradeStatus status) {
        ExchangeNotification exchangeNotification = ExchangeNotification.builder()
                .userId(userId)
                .exchangeItemName(exchangeItemName)
                .requestUserNickname(requesterNickname)
                .tradeStatus(status)
                .build();

        ExchangeNotification savedNotification = exchangeNotificationRepository.save(exchangeNotification);
        applicationEventPublisher.publishEvent(new NotificationEvent<>(NotificationType.EXCHANGE, savedNotification));
    }

    @Transactional
    public void crateChatNotification(Long userId, String content, String requesterNickname, String exchangeItemName, ChatStatus status) {
        ChatNotification chatNotification = ChatNotification.builder()
                .userId(userId)
                .content(content)
                .requestUserNickname(requesterNickname)
                .exchangeItemName(exchangeItemName)
                .chatStatus(status)
                .build();

        ChatNotification savedNotification = chatNotificationRepository.save(chatNotification);
        applicationEventPublisher.publishEvent(new NotificationEvent<>(NotificationType.CHAT, savedNotification));
    }

    @Transactional
    public void createDonationNotification(Long userId, String donationItemName, DonationStatus status) {
        DonationNotification donationNotification = DonationNotification.builder()
                .userId(userId)
                .donationItemName(donationItemName)
                .donationStatus(status)
                .build();

        DonationNotification savedNotification = donationNotificationRepository.save(donationNotification);
        applicationEventPublisher.publishEvent(new NotificationEvent<>(NotificationType.DONATION, savedNotification));
    }
}
