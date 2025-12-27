package edu.controller;

import edu.common.ApiResp;
import edu.dto.LoginReq;
import edu.dto.LoginResp;
import edu.service.LoginService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticateController {

    @Autowired
    private LoginService loginService;

    /**
     * 多因子登录接口
     */
    @PostMapping("/login")
    public ApiResp<LoginResp> login(@RequestBody @Valid LoginReq req) {
        try{
            LoginResp response = loginService.login(req);
            return ApiResp.ok("登录成功", response);
        }catch (Exception e){
            return ApiResp.fail(e.getMessage() == null ? "登录失败" : e.getMessage());
        }
    }
}