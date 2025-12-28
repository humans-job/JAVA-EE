package edu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dto.LoginReq;
import edu.dto.LoginResp;
import edu.service.LoginService;
import edu.session.RedisSessionStore;
import edu.util.Auth0JwtUtil;
import edu.util.MD5Util;
import lombok.RequiredArgsConstructor;
import org.example.army.militarycommon.Entity.User;
import org.example.army.militarycommon.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {
    private final UserMapper userMapper;
    private final RedisSessionStore sessionStore;
    private final ObjectMapper objectMapper;
    private final Auth0JwtUtil auth0JwtUtil;

    @Override
    public LoginResp login(LoginReq req) {
        User user = switch (req.getAuthType()) {
            case 1 -> loginByPassword(req.getUsername(), req.getPassword());
            case 2 -> loginByUsbKey(req.getUsername(), req.getUsbKey());
            case 3 -> loginByCert(req.getUsername(), req.getCertSn());
            default -> throw new IllegalArgumentException("不支持的authType: " + req.getAuthType());
        };

        if (user == null) {
            throw new RuntimeException("认证失败：账号/凭证不正确");
        }
        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new RuntimeException("账号状态异常，禁止登录");
        }

        // 存 Redis（token -> session）
        User session = new User();
        session.setUserId(user.getUserId());
        session.setUsername(user.getUsername());
        session.setUserType(user.getUserType());
        session.setDeptId(user.getDeptId());
        session.setLoginIp(user.getLoginIp());
        session.setLoginTime(LocalDateTime.now());

        try {
            sessionStore.save(user.getUserId(),objectMapper.writeValueAsString(session));
        } catch (Exception e) {
            throw new RuntimeException("会话写入Redis失败");
        }

        // 更新登录时间、IP
        User upd = new User();
        upd.setUserId(user.getUserId());
        upd.setLoginIp(req.getLoginIp());
        upd.setLoginTime(LocalDateTime.now());
        userMapper.updateById(upd);

        // 组装返回
        LoginResp resp = new LoginResp();
        Map<String, Long> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        resp.setToken(auth0JwtUtil.generateToken(claims));
        resp.setUsername(user.getUsername());
        resp.setUserType(user.getUserType());
        return resp;
    }

    private User loginByPassword(String username, String password) {
        if (username.isBlank() || password.isBlank()) return null;
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username).last("limit 1"));
        if (user == null || user.getPassword().isBlank()) return null;
        return MD5Util.verifySaltedMd5(password,user.getPassword()) ? user : null;
    }

    private User loginByUsbKey(String username, String usbKey) {
        if (usbKey.isBlank()) return null;
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<User>()
                .eq(User::getUsbKey, usbKey).last("limit 1");
        // 如果前端也传了 username，就再加一层校验
        if (!username.isBlank()) qw.eq(User::getUsername, username);
        return userMapper.selectOne(qw);
    }

    private User loginByCert(String username, String certSn) {
        if (certSn.isBlank()) return null;
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<User>()
                .eq(User::getCertSn, certSn).last("limit 1");
        if (!username.isBlank()) qw.eq(User::getUsername, username);
        return userMapper.selectOne(qw);
    }
}
