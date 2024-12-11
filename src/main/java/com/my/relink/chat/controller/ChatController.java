package com.my.relink.chat.controller;

import com.my.relink.chat.controller.dto.ChatMessageReqDto;
import com.my.relink.chat.controller.dto.ChatMessageRespDto;
import com.my.relink.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chats/{tradeId}/message")
    public void handleMessage(@DestinationVariable("tradeId") Long tradeId,
                              @Payload ChatMessageReqDto chatMessageReqDto,
                              @Header("simpSessionId") String sessionId) {
        ChatMessageRespDto chatMessageRespDto = chatService.saveAndSendMessage(tradeId, chatMessageReqDto);
        messagingTemplate.convertAndSend("/topic/chat/" + tradeId, response);
    }

}
