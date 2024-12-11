package com.my.relink.chat.controller.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

//클라이언트로부터 받을 채팅 메시지 DTO
@NoArgsConstructor
@Getter
public class ChatMessageReqDto {
    private String content;
    private Long tradeId;
    private String sender;
    private String sentAt;
}
