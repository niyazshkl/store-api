package com.codewithmosh.store.auth;

import com.codewithmosh.store.services.Jwt;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
@Data
public class LoginResponse {
    private Jwt accessToken;
    private Jwt refreshToken;
}
