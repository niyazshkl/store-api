package com.codewithmosh.store.services;

import com.codewithmosh.store.config.JwtConfig;
import com.codewithmosh.store.entities.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Service
@AllArgsConstructor
public class JwtService {
    private JwtConfig jwtConfig;
    public Jwt generateAccessToken(User user){
        return generateJwt(user, jwtConfig.getAccessTokenExpirationInSeconds());
    }
    public Jwt generateRefreshToken(User user){
        return generateJwt(user, jwtConfig.getRefreshTokenExpirationInSeconds());
    }

    private Jwt generateJwt(User user, long expirationInSeconds) {
        JwtBuilder jwtBuilder = Jwts.builder();

        //now we will sign it before building the jwt token
        //but we need a key
        Key secretKey = jwtConfig.getSecretKey();
        jwtBuilder.signWith(secretKey);

        //now we are ready to build the jwt token -> compact() is basically build();

        //AI: how to extract claims from jwtBuilder?
        Claims claims = Jwts.claims()
                .subject(user.getId().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationInSeconds * 1000))
                .add("name", user.getName())
                .add("email", user.getEmail())
                .add("role", user.getRole())
                .build();
        String jwtToken = jwtBuilder.compact();

        return new Jwt(claims, jwtConfig.getSecretKey());
    }
    public Jwt parse(String token) throws Exception {
        JwtParserBuilder parserBuilder = Jwts.parser();
        JwtParser jwtParser = parserBuilder.verifyWith(jwtConfig.getSecretKey()).build();

        //payload = collection of claims where claims are <String, Object>
        // claims -> payload

        Jws<Claims> jws = jwtParser.parseSignedClaims(token); //this can throw

        Claims claims = jws.getPayload();
        return new Jwt(claims, jwtConfig.getSecretKey());
    }

}
