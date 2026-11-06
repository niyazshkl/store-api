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
    public String generateAccessToken(User user){
        return generateJwt(user, jwtConfig.getAccessTokenExpirationInSeconds());
    }
    public String generateRefreshToken(User user){
        return generateJwt(user, jwtConfig.getRefreshTokenExpirationInSeconds());
    }

    private String generateJwt(User user, long expirationInSeconds) {
        JwtBuilder jwtBuilder = Jwts.builder();
        jwtBuilder.subject(user.getId().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationInSeconds * 1000));

        //add custom claims
        jwtBuilder.claim("name", user.getName())
                  .claim("email", user.getEmail())
                  .claim("role", user.getRole());

        //now we will sign it before building the jwt token
        //but we need a key
        Key secretKey = jwtConfig.getSecretKey();
        jwtBuilder.signWith(secretKey);

        //now we are ready to build the jwt token -> compact() is basically build();
        String jwtToken = jwtBuilder.compact();
        return jwtToken;
    }

    public String getRole(String token){
        try {
            Claims claims = parse(token);
            return (String) claims.get("role");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean validateToken(String token){
        Claims claims = null;
        try {
            claims = parse(token);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        if (claims == null) return false;
        return claims.getExpiration().after(new Date());
    }

    private Claims parse(String token) throws Exception {
        JwtParserBuilder parserBuilder = Jwts.parser();
        JwtParser jwtParser = parserBuilder.verifyWith(jwtConfig.getSecretKey()).build();

        //payload = collection of claims where claims are <String, Object>
        // claims -> payload

        Jws<Claims> jws = jwtParser.parseSignedClaims(token); //this can throw

        Claims claims = jws.getPayload();
        return claims;
    }

    public String getSubject(String token){
        try {
            Claims claims = parse(token);
            return claims.getSubject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
