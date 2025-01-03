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
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPublisherService {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ChatNotificationRepository chatNotificationRepository;
    private final DonationNotificationRepository donationNotificationRepository;
    private final ExchangeNotificationRepository exchangeNotificationRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createExchangeNotification(Long userId, String exchangeItemName, String requesterNickname, TradeStatus status) {
        ExchangeNotification exchangeNotification = ExchangeNotification.builder()
                .userId(userId)
                .exchangeItemName(exchangeItemName)
                .requestUserNickname(requesterNickname)
                .tradeStatus(status)
                .build();

        ExchangeNotification savedNotification;

        try {
            savedNotification = exchangeNotificationRepository.save(exchangeNotification);
            applicationEventPublisher.publishEvent(new NotificationEvent<>(NotificationType.EXCHANGE, savedNotification));
        } catch (Exception e) {
            log.warn("알림 발행 실패 : {}", e.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void crateChatNotification(Long userId, String content, String requesterNickname, String exchangeItemName, ChatStatus status) {
        ChatNotification chatNotification = ChatNotification.builder()
                .userId(userId)
                .content(content)
                .requestUserNickname(requesterNickname)
                .exchangeItemName(exchangeItemName)
                .chatStatus(status)
                .build();

        ChatNotification savedNotification;

        try {
            savedNotification = chatNotificationRepository.save(chatNotification);
            applicationEventPublisher.publishEvent(new NotificationEvent<>(NotificationType.CHAT, savedNotification));
        } catch (Exception e) {
            log.warn("알림 발행 실패 : {}", e.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createDonationNotification(Long userId, String donationItemName, DonationStatus status) {
        DonationNotification donationNotification = DonationNotification.builder()
                .userId(userId)
                .donationItemName(donationItemName)
                .donationStatus(status)
                .build();

        DonationNotification savedNotification;

        try {
            savedNotification = donationNotificationRepository.save(donationNotification);
            applicationEventPublisher.publishEvent(new NotificationEvent<>(NotificationType.DONATION, savedNotification));
        } catch (Exception e) {
            log.warn("알림 발행 실패 : {}", e.getMessage());
        }
    }
}
