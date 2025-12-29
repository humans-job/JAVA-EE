package org.example.army.militaryauthenticate.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.army.militaryauthenticate.session.RedisSessionStore;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j // ✅ 1. 引入 Lombok 日志注解
@RequiredArgsConstructor
public class SecurityUtil {

    private final RedisSessionStore sessionStore;
    private final ObjectMapper objectMapper;
    private final HttpServletRequest request;
    private final Auth0JwtUtil auth0JwtUtil;

    /**
     * 获取当前登录用户ID
     */
    public Long getUserId() {
        String token = getTokenFromHeader();

        // ✅ 2. 判空并打印日志
        if (token == null) {
            log.warn("尝试获取UserId失败: Token为空. 请求路径: {}", request.getRequestURI());
            return null;
        }

        Long userId = tokenToUserId(token);

        // 校验 Redis 会话
        String sessionJson = sessionStore.get(userId);
        if (sessionJson == null || sessionJson.isBlank()) {
            log.warn("Redis会话不存在或已过期, UserId: {}", userId);
            throw new RuntimeException("登录态已过期，请重新登录");
        }
        return userId;
    }

    /**
     * 根据 userId 从 Redis 会话中获取部门ID（deptId）
     */
    public Long getDeptId() {
        String token = getTokenFromHeader();

        // ✅ 3. 判空并打印日志（防止后续步骤空指针或Token解析报错）
        if (token == null) {
            log.warn("尝试获取DeptId失败: Token为空. 请求路径: {}", request.getRequestURI());
            return null;
        }

        Long userId = tokenToUserId(token);
        if (userId == null) return null;

        String sessionJson = sessionStore.get(userId);
        if (sessionJson == null || sessionJson.isBlank()) {
            throw new RuntimeException("登录态已过期，请重新登录");
        }

        try {
            JsonNode root = objectMapper.readTree(sessionJson);
            JsonNode deptNode = root.get("deptId");
            if (deptNode == null || deptNode.isNull()) return null;

            // 兼容数字/字符串两种情况
            if (deptNode.canConvertToLong()) return deptNode.asLong();
            String s = deptNode.asText(null);
            return (s == null || s.isBlank()) ? null : Long.valueOf(s);

        } catch (Exception e) {
            log.error("解析Session JSON获取deptId失败", e);
            throw new RuntimeException("解析会话信息失败，无法获取deptId", e);
        }
    }

    /**
     * 从请求头提取 Token
     */
    public String getTokenFromHeader() {
        String auth = request.getHeader("Authorization");
        if (auth == null || auth.isBlank()) {
            // 如果你们用自定义 header，这里再兜底
            auth = request.getHeader("token"); // 或 "X-Token"
        }
        return extractToken(auth);
    }

    private String extractToken(String auth) {
        if (auth == null || auth.isBlank()) return null;
        if (auth.startsWith("Bearer ")) return auth.substring(7).trim();
        return auth.trim();
    }

    /**
     * 解析 Token 获取 UserId
     */
    private Long tokenToUserId(String token) {
        // ✅ 4. 彻底修复：使用传入的 token 参数，而不是 this.getTokenFromHeader()
        if (token == null) return null;

        try {
            // 这里使用了 auth0JwtUtil.verifyToken(token)，确保传入非空值
            return Long.valueOf(auth0JwtUtil.parseUserInfo(
                    auth0JwtUtil.verifyToken(token)
            ).get("userId").toString());
        } catch (Exception e) {
            log.error("Token解析UserId失败: {}", e.getMessage());
            throw new RuntimeException("无效的Token");
        }
    }
}

