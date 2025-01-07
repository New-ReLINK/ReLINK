package com.my.relink.controller.notification.dto.resp;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public abstract class NotificationListRespDto {
    private final LocalDateTime createdAt;
    private final String type;

    public NotificationListRespDto(LocalDateTime createdAt, String type) {
        this.createdAt = createdAt;
        this.type = type;
    }
}
