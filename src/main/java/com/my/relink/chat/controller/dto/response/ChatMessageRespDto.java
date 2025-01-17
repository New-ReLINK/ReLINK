package com.my.relink.chat.controller.dto.response;

import com.my.relink.chat.service.cache.ChatMessage;
import com.my.relink.domain.message.Message;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
/**
 * 서버 -> 클라이언트
 */
@NoArgsConstructor
public class ChatMessageRespDto {

    private String content;
    private Long tradeId;
    private Long senderId;
    private String sentAt;

    public ChatMessageRespDto(Message message) {
        this.content = message.getContent();
        this.tradeId = message.getTrade().getId();
        this.senderId = message.getUser().getId();
        this.sentAt = message.getMessageTime().toString();
    }

    public ChatMessageRespDto(ChatMessage message) {
        this.content = message.getContent();
        this.tradeId = message.getTradeId();
        this.senderId = message.getSenderId();
        this.sentAt = message.getCreatedAt().toString();
    }
}
