package com.my.relink.controller.message.dto.response;

import com.my.relink.domain.message.Message;
import com.my.relink.util.DateTimeUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class MessageRespDto {

    public MessageRespDto(List<Message> messageList, Long nextCursor){
        this(messageList, nextCursor, LocalDateTime.now());
    }

    public MessageRespDto(List<Message> messageList, Long nextCursor, LocalDateTime now) {
        this.messageList = messageList.stream()
                .map(message -> new MessageDto(message, now))
                .toList();
        this.nextCursor = nextCursor;
    }

    private List<MessageDto> messageList;
    private Long nextCursor;

    @Getter
    @NoArgsConstructor
    public static class MessageDto {

        public MessageDto(Message message) {
            this(message, LocalDateTime.now());
        }

        public MessageDto(Message message, LocalDateTime now) {
            this.content = message.getContent();
            this.nickname = message.getUser().getNickname();
            this.userId = message.getUser().getId();
            this.sentAt = DateTimeUtil.getMessageFormattedTime(message.getCreatedAt(), now);
        }

        private String content;
        private String nickname;
        private Long userId;
        private String sentAt;
    }
}
