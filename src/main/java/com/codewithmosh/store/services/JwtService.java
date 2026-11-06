package com.codewithmosh.store.services;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Payload;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service

public class JwtService {
    @Value("${spring.jwt.secret}")
    private String secret;


    public String generateToken(String email){
        JwtBuilder jwtBuilder = Jwts.builder();
        //below is just setting differnt fields inside payload
        long secondsInOneday = 86400;
        jwtBuilder.subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + secondsInOneday * 1000));

        //now we will sign it before building the jwt token
        //but we need a key
        Key secretKey = getSecretKey();
        jwtBuilder.signWith(secretKey);

        //now we are ready to build the jwt token -> compact() is basically build();
        String jwtToken = jwtBuilder.compact();
        return jwtToken;
    }

    private SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(secret.getBytes());
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
        JwtParser jwtParser = parserBuilder.verifyWith(getSecretKey()).build();

        //payload = collection of claims where claims are <String, Object>
        // claims -> payload

        Jws<Claims> jws = jwtParser.parseSignedClaims(token); //this can throw

        Claims claims = jws.getPayload();
        return claims;
    }






}
