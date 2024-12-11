package com.my.relink.chat.controller.dto;

import com.my.relink.domain.message.Message;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Getter
public class ChatMessageReqDto {
    private String content;
    private Long tradeId;
    private String sentAt;

    public Message toEntity(Trade trade, User sender){
        return Message.builder()
                .content(content)
                .user(sender)
                .trade(trade)
                .build();
    }
}
