package com.codewithmosh.store.users;

import com.codewithmosh.store.common.validation.Lowercase;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data //get, set, tostring, hashcode
public class RegisterUserRequestDto{
    @NotBlank
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;
    @NotBlank
    @Lowercase
    @Email(message = "Must be a valid email")
    private String email;
    @Size(min = 6, max = 25, message = "Password must be between 6 and 25 characters")
    private String password;
}