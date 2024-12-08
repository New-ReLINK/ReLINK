package com.my.relink.ex;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND(404, "해당 유저를 찾을 수 없습니다."),
    TOKEN_NOT_FOUND(401, "토큰 정보를 찾을 수 없습니다."),
    MISS_MATCHER_PASSWORD(400, "패스워드가 일치하지 않습니다."),
    JSON_PARSE_ERROR(500, "Json 파싱 중 오류가 발생했습니다."),
    INVALID_CREDENTIALS(401, "아이디 또는 비밀번호가 올바르지 않습니다.")
    ;

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
