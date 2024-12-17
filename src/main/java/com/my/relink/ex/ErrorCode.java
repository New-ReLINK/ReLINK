package com.my.relink.ex;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "해당 유저를 찾을 수 없습니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED.value(), "토큰 정보를 찾을 수 없습니다."),
    MISS_MATCHER_PASSWORD(HttpStatus.BAD_REQUEST.value(), "패스워드가 일치하지 않습니다."),
    JSON_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Json 파싱 중 오류가 발생했습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED.value(), "아이디 또는 비밀번호가 올바르지 않습니다."),
    UNEXPECTED_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST.value(), "유효성 검사 실패"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "요청된 URI를 찾을 수 없습니다"),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "해당 리뷰를 찾을 수 없습니다."),

    TRADE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "해당 거래를 찾을 수 없습니다"),
    TRADE_ACCESS_DENIED(HttpStatus.FORBIDDEN.value(), "해당 거래에 접근 권한이 없습니다"),
    POINT_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "포인트를 찾을 수 없습니다"),
    POINT_SHORTAGE(HttpStatus.FORBIDDEN.value(), "포인트가 부족합니다"),
    POINT_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "포인트 내역을 찾을 수 없습니다"),
    CHATROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN.value(), "거래가 완료되었거나 취소된 채팅방에는 접근할 수 없습니다"),
    TRADE_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "존재하지 않는 거래 상태 입니다"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "카테고리를 찾을 수 없습니다."),
    DEPOSIT_CANNOT_LESS_ZERO(HttpStatus.BAD_REQUEST.value(), "보증금은 0원보다 작을 수 없습니다."),
    DEPOSIT_ALREADY_REFUNDED(HttpStatus.BAD_REQUEST.value(), "이미 환급한 보증금 내역입니다."),
    INVALID_REPORT_REASON(HttpStatus.NOT_FOUND.value(), "해당 신고 사유는 존재하지 않습니다"),
    ALREADY_REPORTED_TRADE(HttpStatus.CONFLICT.value(), "이미 신고된 거래입니다"),

    EXCHANGE_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "해당 교환 상품을 찾을 수 없습니다"),
    NOTIFICATION_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "알람 저장이 실패하였습니다."),
    NOTIFICATION_DELIVERY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "알림 발송에 실패하였습니다."),
    AUTH_FAIL_ERROR(HttpStatus.UNAUTHORIZED.value(), "인증 및 인가에 실패하였습니다."),
    FILTER_CHAIN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "보안 필터 처리 중 오류가 발생했습니다."),
    INVALID_JWT_SIGNATURE(HttpStatus.UNAUTHORIZED.value(), "유효하지 않는 JWT 서명입니다."),
    UNSUPPORTED_JWT_TOKEN(HttpStatus.UNAUTHORIZED.value(), "지원되지 않는 JWT 토큰입니다."),
    INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED.value(), "잘못된 JWT 토큰입니다."),
    EXPIRED_JWT_TOKEN(HttpStatus.FORBIDDEN.value(), "만료된 JWT 토큰입니다."),

    ;
    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
