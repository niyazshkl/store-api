package com.codewithmosh.store.config;

import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
@Data
public class JwtConfig {
    @Value("${spring.jwt.secret}")
    private String secret;
    @Value("${spring.jwt.accessTokenExpirationInSeconds}")
    private int accessTokenExpirationInSeconds;
    @Value("${spring.jwt.refreshTokenExpirationInSeconds}")
    private int refreshTokenExpirationInSeconds;
    public SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
