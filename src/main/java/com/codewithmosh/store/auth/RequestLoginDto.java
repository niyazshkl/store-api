package com.codewithmosh.store.auth;

import lombok.Data;

@Data
public class RequestLoginDto {
    private String email;
    private String password;
}
