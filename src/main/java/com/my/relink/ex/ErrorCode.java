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
    REVIEW_FORBIDDEN(HttpStatus.BAD_REQUEST.value(), "중복되는 리뷰를 작성할 수 없습니다."),

    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "해당 상품을 찾을 수 없습니다."),
    TRADE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "해당 거래를 찾을 수 없습니다"),
    TRADE_ACCESS_DENIED(HttpStatus.FORBIDDEN.value(), "해당 거래에 접근 권한이 없습니다"),
    TRADE_NOT_COMPLETE(HttpStatus.FORBIDDEN.value(), "해당 거래가 끝나지 않아 리뷰를 작성할 수 없습니다."),
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
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED.value(), "해당 상품의 소유자가 아닙니다"),
    ITEM_NOT_AVAILABLE(HttpStatus.BAD_REQUEST.value(), "해당 상품이 교환가능 상태가 아닙니다."),
    INVALID_SORT_PARAMETER(HttpStatus.BAD_REQUEST.value(), "보증금 정렬 기준 값이 올바르지 않습니다."),
    EXCHANGE_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "해당 교환 상품을 찾을 수 없습니다"),
    NOTIFICATION_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "알람 저장이 실패하였습니다."),
    NOTIFICATION_DELIVERY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "알림 발송에 실패하였습니다."),
    AUTH_FAIL_ERROR(HttpStatus.UNAUTHORIZED.value(), "인증 및 인가에 실패하였습니다."),
    FILTER_CHAIN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "보안 필터 처리 중 오류가 발생했습니다."),
    INVALID_JWT_SIGNATURE(HttpStatus.UNAUTHORIZED.value(), "유효하지 않는 JWT 서명입니다."),
    UNSUPPORTED_JWT_TOKEN(HttpStatus.UNAUTHORIZED.value(), "지원되지 않는 JWT 토큰입니다."),
    INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED.value(), "잘못된 JWT 토큰입니다."),
    EXPIRED_JWT_TOKEN(HttpStatus.FORBIDDEN.value(), "만료된 JWT 토큰입니다."),

    POINT_INFO_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "포인트 정보를 찾을 수 없습니다. 고객센터에 문의해주세요"),
    FAIL_TO_POINT_CHARGE(HttpStatus.INTERNAL_SERVER_ERROR.value(), "포인트 충전에 실패했습니다. 잠시 후 다시 시도해주세요"),
    FAIL_TO_UPDATE_PAYMENT_STATUS(HttpStatus.INTERNAL_SERVER_ERROR.value(), "결제 상태 업데이트에 실패했습니다"),
    FAIL_TO_SAVE_PAYMENT(HttpStatus.INTERNAL_SERVER_ERROR.value(), "결제 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요"),
    CRITICAL_PAYMENT_SAVE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "결제 처리 중 심각한 오류가 발생했습니다. 고객센터에 문의해주세요"),
    TOSS_PAYMENT_CANCEL_STATUS_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR.value(), "결제 취소 상태 정보를 찾을 수 없습니다"),
    PAYMENT_CANCEL_INCOMPLETE(HttpStatus.INTERNAL_SERVER_ERROR.value(), "결제 취소가 정상적으로 처리되지 않았습니다"),
    PAYMENT_CANCEL_STATUS_INVALID(HttpStatus.VARIANT_ALSO_NEGOTIATES.value(), "결제 취소 상태가 유효하지 않습니다"),
    PAYMENT_INFO_MISMATCH(HttpStatus.INTERNAL_SERVER_ERROR.value(), "결제 정보가 일치하지 않습니다"),
    UNEXPECTED_FAIL_TO_PAYMENT_CONFIRM(HttpStatus.INTERNAL_SERVER_ERROR.value(), "결제 승인 요청에 실패했습니다. 잠시 후 다시 시도해주세요"),
    CRITICAL_POINT_CHARGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "포인트 충전 중 심각한 오류가 발생했습니다. 고객센터에 문의해주세요"),
    CRITICAL_PAYMENT_PROCESS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "결제 처리 프로세스 중 심각한 오류가 발생했습니다. 고객센터에 문의해주세요"),


    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST.value(), "파일 크기를 초과하였습니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST.value(), "지원하지 않는 파일 형식입니다."),
    ALREADY_IMAGE_FILE(HttpStatus.BAD_REQUEST.value(), "프로필 이미지는 하나만 등록할 수 있습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "이미지를 찾을 수 없습니다."),
    IMAGE_ACCESS_DENIED(HttpStatus.BAD_REQUEST.value(), "해당 이미지의 소유자와 다릅니다."),
    INVALID_FILE_URL(HttpStatus.BAD_REQUEST.value(), "파일 URL 이 비어있습니다."),
    ITEM_IN_EXCHANGE(HttpStatus.BAD_REQUEST.value(), "해당 상품이 거래중입니다."),
    DONATION_ITEM_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR.value(), "해당 기부 상품을 찾을 수 없습니다.")


    ;
    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
