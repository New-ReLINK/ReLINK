package com.my.relink.config.security.jwt;

import com.my.relink.config.security.domain.CustomUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    @Value("${jwt.secret.key}")
    private String secretKey;

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7;
    public static final String TOKEN_PREFIX = "Bearer ";

    private Key key;

    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @PostConstruct
    public void init() {
        byte[] decode = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(decode);
    }


    public String generateToke(Authentication authentication) {
        return TOKEN_PREFIX + Jwts.builder()
                .setSubject(authentication.getName())
                .claim("Role", authentication.getAuthorities().stream().findFirst().toString())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, signatureAlgorithm)
                .compact();
    }

    public void validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("유효하지 않는 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 JWT 토큰입니다.");
        }
    }

    public CustomUserDetails getUserDetailsForToken(String token) {
        Jws<Claims> claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);

        String email = claims.getBody().getSubject();
        String role = (String) claims.getBody().get("Role");

        log.info("Token Claim Email : {}", email);
        log.info("Token Claim Role : {}", role);

        return new CustomUserDetails(email, role);
    }
}
