package com.codewithmosh.store.dtos.carts;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateCartItemRequestDto {
    @Min(value = 1, message = "Must be at least 1")
    @Max(value = 100, message = "Must be less than 100")
    private Integer quantity;
}
