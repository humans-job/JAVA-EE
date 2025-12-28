package edu.util;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtil {

    public static String getClientIp(HttpServletRequest request) {
        // 1) 标准/常用的代理头
        String ip = getHeader(request, "X-Forwarded-For");
        if (ip != null) {
            // X-Forwarded-For 可能是 "client, proxy1, proxy2"
            // 取第一个非 unknown
            String[] parts = ip.split(",");
            for (String p : parts) {
                String candidate = p.trim();
                if (!candidate.isEmpty() && !"unknown".equalsIgnoreCase(candidate)) {
                    return normalize(candidate);
                }
            }
        }

        ip = getHeader(request, "X-Real-IP");
        if (ip != null) return normalize(ip);

        ip = getHeader(request, "Forwarded");
        if (ip != null) {
            // Forwarded: for=1.2.3.4;proto=http;by=...
            // 简单解析 for=
            String lower = ip.toLowerCase();
            int idx = lower.indexOf("for=");
            if (idx >= 0) {
                String sub = ip.substring(idx + 4);
                int end = sub.indexOf(';');
                String candidate = (end >= 0 ? sub.substring(0, end) : sub).trim();
                candidate = candidate.replace("\"", "");
                return normalize(candidate);
            }
        }

        // 2) fallback：直连/最后兜底
        return normalize(request.getRemoteAddr());
    }

    private static String getHeader(HttpServletRequest request, String name) {
        String v = request.getHeader(name);
        if (v == null || v.isBlank() || "unknown".equalsIgnoreCase(v)) return null;
        return v;
    }

    private static String normalize(String ip) {
        // 处理 IPv6 本地回环
        if ("0:0:0:0:0:0:0:1".equals(ip)) return "127.0.0.1";
        // 有些 Forwarded 里可能是 for="[2001:db8::1234]"
        ip = ip.replace("[", "").replace("]", "");
        return ip;
    }
}

