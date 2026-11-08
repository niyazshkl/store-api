package com.codewithmosh.store.users;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
//Serialization: java object -> JSON
public class UserDto {
    @JsonIgnore
    private Long id;
    @JsonProperty("fullName")
    private String name;
    private String email;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String phoneNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime dateTime;

}
