package com.my.relink.chat.controller.dto.request;

import com.my.relink.domain.message.Message;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Getter
/**
 * 클라이언트 -> 서버
 */
@AllArgsConstructor
public class ChatMessageReqDto {
    private String content;
    private Long tradeId;

    public Message toEntity(Trade trade, User sender){
        return Message.builder()
                .content(content)
                .user(sender)
                .trade(trade)
                .build();
    }
}
