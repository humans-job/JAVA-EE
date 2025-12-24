package edu.config;

import edu.controller.NoticeController;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public NoticeController.ApiResp<Boolean> methodNotAllowed(HttpRequestMethodNotSupportedException e) {
        return NoticeController.ApiResp.fail("Method Not Allowed");
    }

    @ExceptionHandler(Exception.class)
    public NoticeController.ApiResp<Boolean> handle(Exception e) {
        // 你也可以把 msg 固定成 "error"，避免把异常信息返回给前端
        return NoticeController.ApiResp.fail("error");
    }
}
