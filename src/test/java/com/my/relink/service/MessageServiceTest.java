package com.my.relink.service;

import com.my.relink.controller.message.dto.response.MessageRespDto;
import com.my.relink.domain.message.Message;
import com.my.relink.domain.message.MessageRepository;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.user.User;
import com.my.relink.util.DummyObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest extends DummyObject {

    @InjectMocks
    private MessageService messageService;

    @Mock
    private MessageRepository messageRepository;


    @Test
    @DisplayName("채팅 내역 조회: 커서가 없을 때 최근 메시지를 조회한다")
    void getChatRoomMessage_success_withNullCursor() {
        User user = mockOwnerUser();
        Trade trade = mockTrade(user, user);
        int size = 10;
        Long cursor = null;
        List<Message> messageList = mockMessageList(trade, user);

        when(messageRepository.findMessagesBeforeCursor(
                eq(trade.getId()),
                eq(Long.MAX_VALUE),
                any(PageRequest.class)
        )).thenReturn(messageList);

        MessageRespDto result = messageService.getChatRoomMessage(trade.getId(), size, cursor);

        assertThat(result.getMessageList()).hasSize(10);
        assertThat(result.getNextCursor()).isEqualTo(11L);
    }



}