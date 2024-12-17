package com.my.relink.client.tosspayments.feature;

public enum PaymentStatus {

    READY("결제 준비"),
    IN_PROGRESS("결제수단 정보 및 소유자 인증 완료"),
    DONE("결제 승인 또는 결제 취소 완료"),
    CANCELED("결제 취소 완료"),
    ABORTED("결제 승인 실패"),
    EXPIRED("거래 취소 (결제 유효 시간 30분 초과)")
    ;

    private final String message;

    PaymentStatus(String message) {
        this.message = message;
    }
}
