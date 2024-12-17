package com.my.relink.domain.payment;


import com.my.relink.domain.BaseEntity;
import com.my.relink.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String method; //결제 수단 ( 카드, 가상계좌, 간편결제, 휴대폰, 계좌이체, 문화상품권, 도서문화상품권, 게임문화상품권 )

    private String provider; //결제 수단이 간편결제일 때, 선택한 간편 결제사 코드 (카카오페이, 네이버페이..)

    private Integer amount;

    private String status; //결제 처리 상태

    private LocalDateTime paidAt; //결제 승인이 일어난 날짜

    @Lob
    private String failReason; //실패 사유

    private String merchantUid; // 주문번호: 식별자

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    public void updateFailInfo(String failReason, String status){
        this.failReason = failReason;
        this.status = status;
    }
}
