package com.codewithmosh.store.carts;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {
    Cart toEntity(CartDto cartDto);
    CartDto toDto(Cart cart);
    @Mapping(target = "totalPrice", expression = "java(cartItem.getCartItemPrice())")
    CartItemDto toDto(CartItem cartItem);
}
