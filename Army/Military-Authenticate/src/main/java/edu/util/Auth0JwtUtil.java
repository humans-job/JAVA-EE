package edu.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class Auth0JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expire}")
    private long expire;

    // 生成JWT令牌
    public String generateToken(Map<String, Long> claims) {
        // 1. 指定签名算法（HS256）
        Algorithm algorithm = Algorithm.HMAC256(secret);

        // 2. 构建令牌：设置过期时间、自定义声明、签名
        return JWT.create()
                .withIssuedAt(new Date())  // 签发时间
                .withExpiresAt(new Date(System.currentTimeMillis() + expire))  // 过期时间
                .withClaim("data", claims)  // 自定义声明（存用户信息，比如userId、username）
                .sign(algorithm);  // 签名生成令牌
    }

    // 验证JWT令牌（返回解析后的DecodedJWT，验证失败抛异常）
    public DecodedJWT verifyToken(String token) throws JWTVerificationException {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        // 创建验证器，自动验证签名和过期时间
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(token);  // 验证通过返回解析结果，失败抛异常
    }

    // 解析令牌中的自定义信息
    public Map<String, Object> parseUserInfo(DecodedJWT decodedJWT) {
        Claim dataClaim = decodedJWT.getClaim("data");
        return dataClaim.asMap();  // 转为Map获取自定义信息
    }
}