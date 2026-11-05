package com.codewithmosh.store.services;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service

public class JwtService {


    public String generateToken(String email){
        JwtBuilder jwtBuilder = Jwts.builder();
        //below is just setting differnt fields inside payload
        long secondsInOneday = 86400;
        jwtBuilder.subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + secondsInOneday * 1000));

        //now we will sign it before building the jwt token
        //but we need a key
        Key key = Keys.hmacShaKeyFor("dbZmxe/9pBQWG8xk3Dok0syFm9dxv/P2ON0utZUE/5I=".getBytes());
        jwtBuilder.signWith(key);

        //now we are ready to build the jwt token -> compact() is basically build();
        String jwtToken = jwtBuilder.compact();
        return jwtToken;
    }

}
