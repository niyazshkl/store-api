package com.codewithmosh.store.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequestDto {
    @NotBlank(message = "oldPassword must not be blank")
    private String oldPassword;
    @Size(min = 6, max = 25, message = "Password must be between 6 and 25 characters")
    private String newPassword;
}
