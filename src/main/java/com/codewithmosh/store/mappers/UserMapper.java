package com.codewithmosh.store.mappers;

import com.codewithmosh.store.dtos.UserDto;
import com.codewithmosh.store.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring") //alows bean generation of a dymanic class implementation at runtime
public interface UserMapper { //mapStruct automatically generates an implementation at runtime
    UserDto toDto(User user);
}
