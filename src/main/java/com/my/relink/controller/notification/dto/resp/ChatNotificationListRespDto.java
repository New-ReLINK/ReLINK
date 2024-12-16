package com.my.relink.controller.notification.dto.resp;

import com.my.relink.domain.notification.chat.ChatNotification;
import lombok.Getter;

@Getter
public class ChatNotificationListRespDto extends NotificationListRespDto {
    private final String requesterNickname;
    private final String message;
    private final String itemName;

    public ChatNotificationListRespDto(ChatNotification notification) {
        super(notification.getCreatedAt(), "CHAT");
        this.requesterNickname = notification.getRequestUserNickname();
        this.message = notification.getContent();
        this.itemName = notification.getExchangeItemName();
    }
}
