package com.codewithmosh.store.services;

import com.codewithmosh.store.entities.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.util.Date;

public class Jwt {
    // Information expert principle.
    private Claims claims;
    private SecretKey key;

    public Jwt(Claims claims, SecretKey key) {
        this.claims = claims;
        this.key = key;
    }

    public boolean isUnexpired(){
        return claims.getExpiration().after(new Date());
    }

    public Object getSubject(){
        return claims.getSubject();
    }

    public Role getRole(){
        Object role = claims.get("role");

        return Role.valueOf(role.toString());
    }


    @Override
    public String toString() {
        return Jwts.builder().claims(claims).signWith(key).compact();
    }
}
