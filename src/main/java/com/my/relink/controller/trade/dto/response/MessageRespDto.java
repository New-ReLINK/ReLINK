package com.my.relink.controller.trade.dto.response;

import com.my.relink.domain.message.Message;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class MessageRespDto {

    public MessageRespDto(List<Message> messageList, Long nextCursor) {
        this.messageList = messageList.stream().map(MessageDto::new).toList();
        this.nextCursor = nextCursor;
    }

    private List<MessageDto> messageList;
    private Long nextCursor;

    @Getter
    @NoArgsConstructor
    public static class MessageDto {
        public MessageDto(Message message) {
            this.content = message.getContent();
            this.nickname = message.getUser().getNickname();
            this.userId = message.getUser().getId();
            this.sentAt = message.getCreatedAt();
        }

        private String content;
        private String nickname;
        private Long userId;
        private LocalDateTime sentAt;
    }
}
