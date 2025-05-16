package com.hk.aiagent.exception;

import com.hk.aiagent.common.ErrorCode;
import com.hk.aiagent.common.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseResult<?> businessExceptionHandler(BusinessException e){
        log.info("BusinessException",e);
        e.printStackTrace();
        return ResponseResult.fail(e.getCode(),e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseResult<?> businessExceptionHandler(RuntimeException e){
        log.info("RuntimeException",e);
        e.printStackTrace();
        return ResponseResult.fail(ErrorCode.ERROR_SYSTEM,e.getMessage());
    }

}
