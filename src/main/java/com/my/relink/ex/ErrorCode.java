package com.my.relink.ex;

import lombok.Getter;

@Getter
public enum ErrorCode {
    ;

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
