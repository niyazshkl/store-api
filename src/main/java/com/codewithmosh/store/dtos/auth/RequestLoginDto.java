package com.codewithmosh.store.dtos.auth;

import lombok.Data;

@Data
public class RequestLoginDto {
    private String email;
    private String password;
}
