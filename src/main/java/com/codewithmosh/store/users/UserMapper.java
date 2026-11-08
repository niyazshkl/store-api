package com.codewithmosh.store.users;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring") //alows bean generation of a dymanic class implementation at runtime
public interface UserMapper { //mapStruct automatically generates an implementation at runtime
    @Mapping(target="dateTime", expression = "java(java.time.LocalDateTime.now())")
    UserDto toDto(User user);

    User toEntity(RegisterUserRequestDto registerUserRequestDto);

    void update(UpdateUserRequest updateUserRequest, @MappingTarget User user);
}
