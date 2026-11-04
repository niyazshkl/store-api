package com.codewithmosh.store.dtos.carts;

import com.codewithmosh.store.entities.CartItem;
import lombok.Data;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
@Data
public class CartDto {
    private UUID id;
    private List<CartItemDto> items;
    private BigDecimal totalPrice;

    public CartDto() {
        this.id = null;
        this.items = new ArrayList<>();
        this.totalPrice = BigDecimal.ZERO;
    }
}
