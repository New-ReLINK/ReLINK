package com.my.relink.service;

import com.my.relink.controller.message.dto.response.MessageRespDto;
import com.my.relink.domain.message.Message;
import com.my.relink.domain.message.MessageRepository;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.user.User;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import com.my.relink.util.DateTimeUtil;
import com.my.relink.util.DummyObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest extends DummyObject {

    @InjectMocks
    private MessageService messageService;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private TradeService tradeService;

    @Mock
    private DateTimeUtil dateTimeUtil;

    @Nested
    @DisplayName("채팅 메시지 내역 조회 테스트")
    class GetChatRoomMessage{
        private final Long tradeId = 1L;
        private final int size = 10;
        private final Long cursor = 100L;
        private final int DEFAULT_PAGE = 0;

        private Trade trade = mock(Trade.class);

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase{

            @Test
            @DisplayName("채팅 메시지 내역을 정상적으로 조회한다")
            void getChatRoomMessage_success() {
                List<Message> messageList = createMessageList(size+1);

                when(tradeService.findByIdOrFail(tradeId)).thenReturn(trade);
                when(messageRepository.findMessagesBeforeCursor(
                        eq(tradeId),
                        eq(cursor),
                        eq(PageRequest.of(DEFAULT_PAGE, size+1))
                )).thenReturn(messageList);

                MessageRespDto result = messageService.getChatRoomMessages(tradeId, size, cursor);

                assertAll(
                        () -> assertThat(result.getMessageList()).hasSize(size),
                        () -> assertThat(result.getNextCursor()).isEqualTo(messageList.get(size - 1).getId()),
                        () -> verify(messageRepository).findMessagesBeforeCursor(
                                eq(tradeId),
                                eq(cursor),
                                eq(PageRequest.of(DEFAULT_PAGE, size+1))
                        )
                );

            }

            @Test
            @DisplayName("cursor가 null일 때 MAX_VALUE로 설정하여 조회한다")
            void getChatRoomMessage_whenCursorIsNull(){
                List<Message> messageList = createMessageList(size+1);

                when(tradeService.findByIdOrFail(tradeId)).thenReturn(trade);
                when(messageRepository.findMessagesBeforeCursor(
                        eq(tradeId),
                        eq(Long.MAX_VALUE),
                        eq(PageRequest.of(DEFAULT_PAGE, size+1))
                )).thenReturn(messageList);

                MessageRespDto result = messageService.getChatRoomMessages(tradeId, size, null);

                assertAll(
                        () -> assertThat(result.getMessageList()).hasSize(size),
                        () -> assertThat(result.getNextCursor()).isEqualTo(messageList.get(size - 1).getId()),
                        () -> verify(messageRepository).findMessagesBeforeCursor(
                                eq(tradeId),
                                eq(Long.MAX_VALUE),
                                eq(PageRequest.of(DEFAULT_PAGE, size+1))
                        )
                );

            }

            @Test
            @DisplayName("메시지 개수가 페이지 크기보다 적을 때 다음 커서로 null을 반환한다")
            void getChatRoomMessages_return_allMessages_and_nullCursor(){
                List<Message> messageList = createMessageList(size - 4); //6개

                when(tradeService.findByIdOrFail(tradeId)).thenReturn(trade);
                when(messageRepository.findMessagesBeforeCursor(
                        eq(tradeId),
                        eq(cursor),
                        eq(PageRequest.of(DEFAULT_PAGE, size+1))
                )).thenReturn(messageList);

                MessageRespDto result = messageService.getChatRoomMessages(tradeId, size, cursor);

                assertAll(
                        () -> assertThat(result.getMessageList()).hasSize(size - 4),
                        () -> assertThat(result.getNextCursor()).isEqualTo(null)
                );
            }

            @Test
            @DisplayName("반환할 메시지 내역이 없는 경우 빈 페이지와 null을 반환한다")
            void getChatRoomMessages_returnEmptyPage_and_nullCursor(){
                List<Message> messageList = createMessageList(0);

                when(tradeService.findByIdOrFail(tradeId)).thenReturn(trade);
                when(messageRepository.findMessagesBeforeCursor(
                        eq(tradeId),
                        eq(cursor),
                        eq(PageRequest.of(DEFAULT_PAGE, size+1))

                )).thenReturn(messageList);

                MessageRespDto result = messageService.getChatRoomMessages(tradeId, size, cursor);

                assertAll(
                        () -> assertThat(result.getMessageList()).hasSize(0),
                        () -> assertThat(result.getNextCursor()).isEqualTo(null)
                );
            }


            private List<Message> createMessageList(int size) {
                return IntStream.range(0, size)
                        .mapToObj(i -> {
                            return Message.builder()
                                        .content("test")
                                        .user(User.builder()
                                                .nickname("nickname")
                                                .id(1L)
                                                .build()
                                        )
                                        .id((long) (100 - i))
                                        .build();
                        })
                        .collect(Collectors.toList());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCase{
            private final Long tradeId = 1L;
            private final int size = 10;
            private final Long cursor = 100L;
            private final int DEFAULT_PAGE = 0;

            private Trade trade = mock(Trade.class);

            @Test
            @DisplayName("거래가 존재하지 않을 때 예외가 발생한다")
            void getChatRoomMessages_whenTradeNotFound_throwsException(){
                doThrow(new BusinessException(ErrorCode.TRADE_NOT_FOUND))
                        .when(tradeService)
                        .findByIdOrFail(tradeId);

                assertThatThrownBy(() -> messageService.getChatRoomMessages(tradeId, size, cursor))
                        .isInstanceOf(BusinessException.class);

                verify(messageRepository, never()).findMessagesBeforeCursor(anyLong(), anyLong(), any(Pageable.class));

            }
        }
    }



}