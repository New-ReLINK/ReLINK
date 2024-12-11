package com.my.relink.chat.controller.dto;

//클라이언트에게 보낼 채팅 메시지 DTO
public class ChatMessageRespDto {

    private String content;
    private Long tradeId;
    private String sender;
    private String sentAt;
    private MessageType messageType;

}
