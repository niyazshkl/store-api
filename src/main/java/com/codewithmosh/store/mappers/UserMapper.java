package com.codewithmosh.store.mappers;

import com.codewithmosh.store.dtos.RegisterUserRequestDto;
import com.codewithmosh.store.dtos.UserDto;
import com.codewithmosh.store.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring") //alows bean generation of a dymanic class implementation at runtime
public interface UserMapper { //mapStruct automatically generates an implementation at runtime
    @Mapping(target="dateTime", expression = "java(java.time.LocalDateTime.now())")
    UserDto toDto(User user);

    User toEntity(RegisterUserRequestDto registerUserRequestDto);
}
