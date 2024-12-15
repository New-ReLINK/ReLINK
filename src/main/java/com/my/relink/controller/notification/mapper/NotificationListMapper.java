package com.my.relink.controller.notification.mapper;

import com.my.relink.controller.notification.dto.resp.ChatNotificationListRespDto;
import com.my.relink.controller.notification.dto.resp.DonationNotificationListRespDto;
import com.my.relink.controller.notification.dto.resp.ExchangeNotificationListRespDto;
import com.my.relink.controller.notification.dto.resp.NotificationListRespDto;
import com.my.relink.domain.notification.Notification;
import com.my.relink.domain.notification.chat.ChatNotification;
import com.my.relink.domain.notification.donation.DonationNotification;
import com.my.relink.domain.notification.exchange.ExchangeNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class NotificationListMapper {

    public static Page<NotificationListRespDto> from(Page<Notification> notifications, Pageable pageable) {
        return new PageImpl<>(
                notifications.getContent().stream()
                        .map(NotificationListMapper::convertToDetailDto)
                        .collect(Collectors.toList()),
                pageable,
                notifications.getTotalElements()
        );
    }

    private static NotificationListRespDto convertToDetailDto(Notification notification) {
        if (notification instanceof ChatNotification chat) {
            return new ChatNotificationListRespDto(chat);
        }
        if (notification instanceof ExchangeNotification exchange) {
            return new ExchangeNotificationListRespDto(exchange);
        }
        if (notification instanceof DonationNotification donation) {
            return new DonationNotificationListRespDto(donation);
        }
        return null;
    }
}
