package com.my.relink.util.api;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.my.relink.ex.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"success", "data", "error"})
public class ApiResult<T> {
    private final T data;
    private final boolean success;
    private final ApiError error;

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(data, true, null);
    }

    public static <T> ApiResult<T> error(ErrorCode errorCode) {
        return new ApiResult<>(null, false, new ApiError(errorCode.getMessage(), errorCode.getStatus()));
    }

    public static <T> ApiResult<T> error(T errorData, ErrorCode errorCode) {
        return new ApiResult<>(errorData, false, new ApiError(errorCode.getMessage(), errorCode.getStatus()));
    }
}
