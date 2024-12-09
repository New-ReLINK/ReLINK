package com.my.relink.ex;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "카테고리를 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "유저를 찾을 수 없습니다"),
    DEPOSIT_CANNOT_LESS_ZERO(HttpStatus.BAD_REQUEST.value(), "보증금은 0원보다 작을 수 없습니다.")
    ;

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
