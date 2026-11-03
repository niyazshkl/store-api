package com.codewithmosh.store.dtos.users;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data //get, set, tostring, hashcode
public class RegisterUserRequestDto{
    @NotBlank
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;
    @NotBlank
    @Email(message = "Must be a valid email")
    private String email;
    @Min(value = 6, message = "At least 6")
    @Max(value = 25, message = "Max 25")
    private String password;
}