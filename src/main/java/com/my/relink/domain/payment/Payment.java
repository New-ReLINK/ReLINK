package com.my.relink.domain.payment;


import com.my.relink.domain.BaseEntity;
import com.my.relink.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String method; //나중에 내려주는 거 보고 enum으로 하던가...

    private Integer amount;

    private String status; //결제 상태

    private LocalDateTime paidAt;

    @Lob
    private String failReason;

    private String merchantUid; // 이것도 일단 내려주는 거 보고 길이 결정해주세용
}
