package org.example.army.militaryauthenticate.controller;

import org.example.army.militaryauthenticate.common.ApiResp;
import org.example.army.militaryauthenticate.dto.LoginReq;
import org.example.army.militaryauthenticate.dto.LoginResp;
import org.example.army.militaryauthenticate.service.LoginService;
import org.example.army.militaryauthenticate.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
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
    public ApiResp<LoginResp> login(@RequestBody @Valid LoginReq req, HttpServletRequest request) {
        try{
            String ip = IpUtil.getClientIp(request);
            req.setLoginIp(ip);
            LoginResp response = loginService.login(req);
            return ApiResp.ok("登录成功", response);
        }catch (Exception e){
            return ApiResp.fail(e.getMessage() == null ? "登录失败" : e.getMessage());
        }
    }
}