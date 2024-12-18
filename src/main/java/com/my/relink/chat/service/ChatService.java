package com.my.relink.chat.service;

import com.my.relink.chat.controller.dto.request.ChatImageReqDto;
import com.my.relink.chat.controller.dto.request.ChatMessageReqDto;
import com.my.relink.chat.controller.dto.response.ChatImageRespDto;
import com.my.relink.chat.controller.dto.response.ChatMessageRespDto;
import com.my.relink.config.s3.S3Service;
import com.my.relink.domain.message.Message;
import com.my.relink.domain.message.repository.MessageRepository;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.user.User;
import com.my.relink.service.TradeService;
import com.my.relink.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final MessageRepository messageRepository;
    private final TradeService tradeService;
    private final UserService userService;
    private final S3Service s3Service;

    @Transactional
    public ChatMessageRespDto saveMessage(Long tradeId, ChatMessageReqDto chatMessageReqDto, Long senderId) {
        User sender = userService.findByIdOrFail(senderId);
        Trade trade = tradeService.findByIdOrFail(tradeId);
        Message message = messageRepository.save(chatMessageReqDto.toEntity(trade, sender));
        return new ChatMessageRespDto(message);
    }

    public void deleteChatsByTradeId(Long tradeId) {
        messageRepository.deleteByTradeId(tradeId);
    }


    public ChatImageRespDto saveImageForChat(Long tradeId, ChatImageReqDto chatImageReqDto, Long userId) {

    }
}
