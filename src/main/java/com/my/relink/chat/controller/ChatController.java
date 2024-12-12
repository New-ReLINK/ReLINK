package com.my.relink.chat.controller;

import com.my.relink.chat.config.ChatPrincipal;
import com.my.relink.chat.controller.dto.ChatMessageReqDto;
import com.my.relink.chat.controller.dto.ChatMessageRespDto;
import com.my.relink.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chats/{tradeId}/message")
    public void handleMessage(@DestinationVariable("tradeId") Long tradeId,
                              @Payload ChatMessageReqDto chatMessageReqDto,
                              Principal principal) {
        ChatMessageRespDto response = chatService.saveMessage(
                        tradeId,
                        chatMessageReqDto,
                        ((ChatPrincipal)principal).getUserId());
        messagingTemplate.convertAndSend("/topic/chats/" + tradeId, response);
    }

}
