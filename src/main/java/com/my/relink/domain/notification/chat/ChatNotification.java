package com.my.relink.domain.notification.chat;

import com.my.relink.domain.notification.Notification;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@DiscriminatorValue(value = "CHAT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatNotification extends Notification {

    private String content;

    @Column(length = 20, nullable = false)
    private String requestUserNickname;

    @Enumerated(EnumType.STRING)
    private ChatStatus chatStatus;

    @Column(length = 30)
    private String exchangeItemName;

    @Builder
    public ChatNotification(Long userId, String content, String requestUserNickname, String exchangeItemName, ChatStatus chatStatus) {
        super(userId);
        this.content = content;
        this.requestUserNickname = requestUserNickname;
        this.chatStatus = chatStatus;
        this.exchangeItemName = exchangeItemName;
    }
}
