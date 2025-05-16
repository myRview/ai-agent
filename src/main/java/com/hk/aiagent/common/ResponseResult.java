package com.hk.aiagent.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseResult<T> {


    private Integer code;

    private String message;

    private T data;


    public static ResponseResult<?> success(String message) {
        return success(message, null);
    }

    public static ResponseResult<?> success() {
        return new ResponseResult<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), null);
    }

    public static <T> ResponseResult<T> success(T data, String message) {
        return new ResponseResult<>(ErrorCode.SUCCESS.getCode(), message, data);
    }

    public static <T> ResponseResult<T> success(T data) {
        return success(data, ErrorCode.SUCCESS.getMessage());
    }

    public static ResponseResult<?> fail(String message) {
        return new ResponseResult<>(ErrorCode.ERROR_SYSTEM.getCode(), message, null);
    }

    public static ResponseResult<?> fail() {
        return fail(ErrorCode.ERROR_SYSTEM.getCode(), ErrorCode.ERROR_SYSTEM.getMessage(), null);
    }

    public static <T> ResponseResult<T> fail(ErrorCode errorCode, String message, T data) {
        return fail(errorCode.getCode(), message, data);
    }

    public static <T> ResponseResult<T> fail(Integer code, String message, T data) {
        return new ResponseResult<>(code, message, data);
    }

    public static <T> ResponseResult<T> fail(ErrorCode errorCode, String message) {
        return fail(errorCode, message, null);
    }

    public static <T> ResponseResult<T> fail(ErrorCode errorCode) {
        return fail(errorCode, errorCode.getMessage(), null);
    }


    public static <T> ResponseResult<T> fail(Integer code, String message) {
        return fail(code, message, null);
    }

}
