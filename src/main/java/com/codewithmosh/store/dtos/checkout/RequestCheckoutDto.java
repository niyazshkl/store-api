package com.codewithmosh.store.dtos.checkout;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class RequestCheckoutDto {
    @NotNull
    private UUID cartId;
}
