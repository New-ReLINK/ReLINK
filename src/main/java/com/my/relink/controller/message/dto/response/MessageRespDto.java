package com.my.relink.controller.message.dto.response;

import com.my.relink.domain.message.Message;
import com.my.relink.util.DateTimeUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class MessageRespDto {

    public MessageRespDto(List<Message> messageList, Long nextCursor, DateTimeUtil dateTimeUtil) {
        this.messageList = messageList.stream()
                .map(message -> new MessageDto(message, dateTimeUtil))
                .toList();
        this.nextCursor = nextCursor;
    }

    private List<MessageDto> messageList;
    private Long nextCursor;

    @Getter
    @RequiredArgsConstructor
    public static class MessageDto {

        public MessageDto(Message message, DateTimeUtil dateTimeUtil) {
            this.content = message.getContent();
            this.nickname = message.getUser().getNickname();
            this.userId = message.getUser().getId();
            this.sentAt = dateTimeUtil.getMessageFormattedTime(message.getCreatedAt());
        }

        private String content;
        private String nickname;
        private Long userId;
        private String sentAt;
    }
}
