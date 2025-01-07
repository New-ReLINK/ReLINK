package com.my.relink.chat.service.cache;

import com.my.relink.domain.message.Message;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.user.User;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@RedisHash("ChatMessage")
@Getter
@Setter
@NoArgsConstructor
@Data
public class ChatMessage implements Serializable {

    private String messageIdentifier;
    private String content;
    private Long senderId;
    private Long tradeId;
    private LocalDateTime createdAt;

    public ChatMessage(Message message, LocalDateTime createdAt) {
        this.messageIdentifier = UUID.randomUUID().toString();
        this.content = message.getContent();
        this.senderId = message.getUser().getId();
        this.tradeId = message.getTrade().getId();
        this.createdAt = createdAt;
    }

    public Message toEntity(){
        return Message.createWithTime(
                Trade.builder().id(tradeId).build(),
                User.builder().id(senderId).build(),
                content,
                createdAt
        );
    }

}
