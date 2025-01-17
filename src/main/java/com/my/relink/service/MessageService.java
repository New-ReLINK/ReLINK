package com.my.relink.service;

import com.my.relink.controller.message.dto.response.MessageRespDto;
import com.my.relink.domain.message.Message;
import com.my.relink.domain.message.repository.MessageRepository;
import com.my.relink.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final TradeService tradeService;
    private final DateTimeUtil dateTimeUtil;
    private static final Long DEFAULT_CURSOR = Long.MAX_VALUE;
    private static final int DEFAULT_PAGE = 0;
    private final Clock clock;


    /**
     * 채팅방 이전 대화 내역 조회하기
     * 커서 기반 페이징 진행
     * @param tradeId
     * @param size
     * @param cursor
     * @return
     */
    public MessageRespDto getChatRoomMessages(Long tradeId, int size, Long cursor) {
        tradeService.findByIdOrFail(tradeId);
        LocalDateTime timestampCursor = getInitialCursor(cursor);

        List<Message> messageList = messageRepository.findMessagesBeforeCursor(
                tradeId,
                timestampCursor,
                PageRequest.of(DEFAULT_PAGE, size + 1)
        );
        Long nextCursor = getNextCursor(messageList, size);
        List<Message> pageMessageList = limitMessages(messageList, size);

        return new MessageRespDto(pageMessageList, nextCursor, dateTimeUtil);
    }

    private Long getNextCursor(List<Message> messageList, int size) {
        if (messageList.size() <= size) {
            return null;
        }
        return messageList.get(size).getMessageTime()
                .atZone(clock.getZone())
                .toInstant()
                .toEpochMilli();
    }

    private List<Message> limitMessages(List<Message> messages, int size) {
        return messages.size() > size ? messages.subList(0, size) : messages;
    }

    private LocalDateTime getInitialCursor(Long cursor) {
        return cursor == null ?
                LocalDateTime.now(clock) :
                LocalDateTime.ofInstant(Instant.ofEpochMilli(cursor), clock.getZone());
    }
}
