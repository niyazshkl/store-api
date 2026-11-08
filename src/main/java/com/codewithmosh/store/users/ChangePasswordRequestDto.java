package com.codewithmosh.store.users;

import lombok.Data;

@Data
public class ChangePasswordRequestDto {
    private String oldPassword;
    private String newPassword;
}
