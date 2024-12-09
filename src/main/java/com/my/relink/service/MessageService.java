package com.my.relink.service;

import com.my.relink.controller.message.dto.response.MessageRespDto;
import com.my.relink.domain.message.Message;
import com.my.relink.domain.message.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {
    private final MessageRepository messageRepository;

    /**
     * 채팅방 이전 대화 내역 조회하기
     * 커서 기반 페이징 진행
     * @param tradeId
     * @param size
     * @param cursor
     * @return
     */
    public MessageRespDto getChatRoomMessage(Long tradeId, int size, Long cursor) {
        // cursor에 값이 없을 시 가장 최근 메시지부터 조회 위해 값 설정
        cursor = (cursor == null || cursor == 0) ? Long.MAX_VALUE : cursor;

        List<Message> messageList = messageRepository.findMessagesBeforeCursor(tradeId, cursor, PageRequest.of(0, size + 1));
        List<Message> pageMessageList = messageList.size() > size
                ? messageList.subList(0, size)
                : messageList;
        Long nextCursor = !pageMessageList.isEmpty()
                ? pageMessageList.get(pageMessageList.size() - 1).getId()
                : null;

        return new MessageRespDto(pageMessageList, nextCursor);
    }
}
