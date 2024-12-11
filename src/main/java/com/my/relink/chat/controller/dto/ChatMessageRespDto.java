package com.my.relink.chat.controller.dto;

import com.my.relink.domain.message.Message;
import lombok.Getter;

@Getter
public class ChatMessageRespDto {

    private String content;
    private Long tradeId;
    private Long senderId;
    private String sentAt;
    private MessageType messageType;

    public ChatMessageRespDto(Message message, MessageType messageType) {
        this.content = message.getContent();
        this.tradeId = message.getTrade().getId();
        this.senderId = message.getUser().getId();
        this.sentAt = message.getCreatedAt().toString();
        this.messageType = messageType;
    }
}
