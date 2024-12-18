package com.my.relink.common.notification;

import com.my.relink.domain.notification.Notification;

public record NotificationEvent<T extends Notification>(NotificationType type, T data) {
}
