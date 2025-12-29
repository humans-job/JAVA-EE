package org.example.army.militaryauthenticate.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.army.militaryauthenticate.session.RedisSessionStore;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final RedisSessionStore sessionStore;
    private final ObjectMapper objectMapper;
    private final HttpServletRequest request;
    private final Auth0JwtUtil auth0JwtUtil;

    public Long getUserId() {
        String token = getTokenFromHeader();
        if (token == null) return null;
        Long userId = tokenToUserId(token);
        String sessionJson = sessionStore.get(userId);
        if (sessionJson == null || sessionJson.isBlank()) {
            throw new RuntimeException("登录态已过期，请重新登录");
        }
        return userId;
    }

    /**
     * 根据 userId 从 Redis 会话中获取部门ID（deptId）
     */
    public Long getDeptId() {
        Long userId = tokenToUserId(getTokenFromHeader());
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
            throw new RuntimeException("解析会话信息失败，无法获取deptId", e);
        }
    }

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

    private Long tokenToUserId(String token) {
        return Long.valueOf(auth0JwtUtil.parseUserInfo(auth0JwtUtil.verifyToken(this.getTokenFromHeader())).get("userId").toString());
    }
}
