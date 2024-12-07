package com.my.relink.ex;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND(404, "해당 유저를 찾을 수 없습니다."),
    TOKEN_NOT_FOUND(401, "토큰 정보를 찾을 수 없습니다.")
    ;

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
