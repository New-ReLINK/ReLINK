package com.my.relink.controller.message;

import com.my.relink.controller.message.dto.response.MessageRespDto;
import com.my.relink.domain.message.Message;
import com.my.relink.domain.user.User;
import com.my.relink.service.MessageService;
import com.my.relink.util.DateTimeUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    MessageService messageService;

    @Nested
    @DisplayName("채팅 메시지 내역 조회 테스트")
    class getChatRoomMessages{
        private final Long tradeId = 1L;
        private final int size = 10;


        private final LocalDateTime FIXED_NOW = LocalDateTime.now();


        //fixed_now 기준으로 설정
        private final LocalDateTime TODAY_MESSAGE = FIXED_NOW.minusHours(2);        //같은 날 (오전 10시)
        private final LocalDateTime YESTERDAY_MESSAGE = FIXED_NOW.minusDays(1);     //어제
        private final LocalDateTime LAST_MONTH_MESSAGE = FIXED_NOW.minusMonths(1);  //지난달
        private final LocalDateTime LAST_YEAR_MESSAGE = FIXED_NOW.minusYears(1);    //작년


        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase{
            @Test
            @DisplayName("cursor가 없으면 첫 페이지를 조회한다")
            @WithMockUser
            void getChatRoomMessages_without_cursor() throws Exception {
                MessageRespDto response = createMessageRespDtoWithDifferentTimes();
                when(messageService.getChatRoomMessages(tradeId, size, null))
                        .thenReturn(response);

                ResultActions resultActions = mvc.perform(get("/chat/{tradeId}/messages", tradeId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.messageList[0].content").value("테스트 메시지"))
                        .andExpect(jsonPath("$.data.messageList[0].sentAt").value(DateTimeUtil.getMessageFormattedTime(TODAY_MESSAGE, FIXED_NOW)))
                        .andExpect(jsonPath("$.data.nextCursor").value(response.getNextCursor()));

                String responseBody = resultActions.andReturn().getResponse().getContentAsString();

                System.out.println("responseBody = " + responseBody);
            }

            @Test
            @DisplayName("다양한 시간대의 메시지 포맷팅이 적용된다")
            @WithMockUser
            void getChatRoomMessages_different_times() throws Exception {
                MessageRespDto response = createMessageRespDtoWithDifferentTimes();
                when(messageService.getChatRoomMessages(tradeId, size, null))
                        .thenReturn(response);

                ResultActions resultActions = mvc.perform(get("/chat/{tradeId}/messages", tradeId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.messageList[0].sentAt").value(DateTimeUtil.getMessageFormattedTime(TODAY_MESSAGE, FIXED_NOW)))
                        .andExpect(jsonPath("$.data.messageList[1].sentAt").value(DateTimeUtil.getMessageFormattedTime(YESTERDAY_MESSAGE, FIXED_NOW)))
                        .andExpect(jsonPath("$.data.messageList[2].sentAt").value(DateTimeUtil.getMessageFormattedTime(LAST_MONTH_MESSAGE, FIXED_NOW)))
                        .andExpect(jsonPath("$.data.messageList[3].sentAt").value(DateTimeUtil.getMessageFormattedTime(LAST_YEAR_MESSAGE, FIXED_NOW)));

                String responseBody = resultActions.andReturn().getResponse().getContentAsString();
                System.out.println("responseBody = " + responseBody);
            }

            private MessageRespDto createMessageRespDtoWithDifferentTimes() {
                List<Message> messages = new ArrayList<>();

                messages.add(createMessage(TODAY_MESSAGE));
                messages.add(createMessage(YESTERDAY_MESSAGE));
                messages.add(createMessage(LAST_MONTH_MESSAGE));
                messages.add(createMessage(LAST_YEAR_MESSAGE));

                return new MessageRespDto(messages, 999L, FIXED_NOW);
            }


            private Message createMessage(LocalDateTime time) {
                Message message = Message.builder()
                        .content("테스트 메시지")
                        .user(User.builder()
                                .id(1L)
                                .nickname("maeda")
                                .build())
                        .build();

                ReflectionTestUtils.setField(message, "createdAt", time);

                return message;
            }

        }

        @Nested
        @DisplayName("실패 케이스")
        class FailureCase{

            @Test
            @DisplayName("size가 1-100 범위를 벗어나면 400을 응답한다")
            @WithMockUser
            void getChatRoomMessages_withInvalidSizeParameter() throws Exception {
                mvc.perform(get("/chat/{tradeId}/messages", tradeId)
                                .param("size", "0"))
                        .andExpect(status().isBadRequest());

                mvc.perform(get("/chat/{tradeId}/messages", tradeId)
                                .param("size", "101"))
                        .andExpect(status().isBadRequest());
            }


        }

    }
}