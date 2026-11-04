package com.codewithmosh.store.mappers;

import com.codewithmosh.store.dtos.carts.CartDto;
import com.codewithmosh.store.entities.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {
    Cart toEntity(CartDto cartDto);
    CartDto toDto(Cart cart);
}
