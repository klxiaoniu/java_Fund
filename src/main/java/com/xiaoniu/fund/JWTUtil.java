package com.xiaoniu.fund;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.xiaoniu.fund.entity.User;
import com.xiaoniu.fund.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JWTUtil {
    // 秘钥
    private static final String SECRET = "XI*AO&NI<U%@4";

    private final Integer JWT_EXPIRE_TIME = 31;

    @Autowired
    UserService userService;

    public String sign(User user) {
        String token = null;
        try {
            Date expireAt = Date.from(LocalDateTime.now().plusDays(JWT_EXPIRE_TIME).atZone(ZoneId.systemDefault()).toInstant());
            token = JWT.create()
                    .withClaim("userId", user.getId())
                    .withClaim("email", user.getEmail())
                    .withExpiresAt(expireAt)
                    .sign(Algorithm.HMAC256(SECRET));
        } catch (Exception e) {
            //log.error(e.getMessage(), e);
        }
        return token;
    }

    /**
     * 签名验证
     * @param token
     * @return
     */
    public boolean verify(String token) {
        try {
            // 0、验签
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET)).build();
            DecodedJWT jwt = verifier.verify(token);

            // 1、提取信息

            // 2、放入到ThreadLocal中，与当前线程绑定。
            // ...
            return true;
        } catch (Exception e) {
            //log.error(e.getMessage(), e);
            return false;
        }
    }

    public User getUserByToken(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET)).build();
        DecodedJWT jwt = verifier.verify(token);

        /*User user = new User();
        user.setId(jwt.getClaim("userId").asLong());
        user.setEmail(jwt.getClaim("email").asString());*/

        return userService.getUserById(jwt.getClaim("userId").asLong());    //TODO:更完善
    }

}
