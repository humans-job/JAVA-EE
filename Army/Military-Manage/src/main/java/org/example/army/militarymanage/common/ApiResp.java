package org.example.army.militarymanage.common;

import lombok.Data;

/**
 * 统一接口返回：
 * {code:200, msg:"...", data:...}
 */
@Data
public class ApiResp<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> ApiResp<T> ok(T data) {
        ApiResp<T> r = new ApiResp<>();
        r.code = 200;
        r.msg = "success";
        r.data = data;
        return r;
    }

    public static <T> ApiResp<T> ok(String msg, T data) {
        ApiResp<T> r = new ApiResp<>();
        r.code = 200;
        r.msg = msg;
        r.data = data;
        return r;
    }

    public static <T> ApiResp<T> fail(String msg) {
        ApiResp<T> r = new ApiResp<>();
        r.code = 500;
        r.msg = msg;
        r.data = null;
        return r;
    }
}
