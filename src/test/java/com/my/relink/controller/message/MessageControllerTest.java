package com.my.relink.controller.message;

import com.my.relink.service.MessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    MessageService messageService;

    @Nested
    @DisplayName("채팅 메시지 내역 조회 테스트")
    class getChatRoomMessages{
        private final Long tradeId = 1L;
        private final LocalDateTime NOW = LocalDateTime.of(2024, 1, 1, 12, 0);
        private final LocalDateTime MESSAGE_TIME = LocalDateTime.of(2024, 1, 1, 11, 30);

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase{

        }

    }
}