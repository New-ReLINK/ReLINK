package com.my.relink.chat.service;

import com.my.relink.chat.controller.dto.ChatMessageReqDto;
import com.my.relink.chat.controller.dto.ChatMessageRespDto;
import com.my.relink.domain.message.repository.MessageRepository;
import com.my.relink.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.hibernate.service.JavaServiceLoadable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final MessageRepository messageRepository;
    private final TradeService tradeService;

    public ChatMessageRespDto saveAndSendMessage(Long tradeId, ChatMessageReqDto chatMessageReqDto) {
        tradeService.findBy

    }

}
