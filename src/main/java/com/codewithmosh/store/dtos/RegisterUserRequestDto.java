package com.codewithmosh.store.dtos;

import lombok.Data;

@Data //get, set, tostring, hashcode
public class RegisterUserRequestDto{
    private String name;
    private String email;
    private String password;
}