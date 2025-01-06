package com.my.relink.domain.message;


import com.my.relink.domain.BaseEntity;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
public class Message extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id")
    private Trade trade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(updatable = false)
    private LocalDateTime messageTime;

    public static Message createWithTime(Trade trade, User user, String content, LocalDateTime createdAt) {
        Message message = Message.builder()
                .trade(trade)
                .user(user)
                .content(content)
                .messageTime(createdAt)
                .build();
        return message;
    }

}
