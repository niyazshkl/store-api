package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.RegisterUserRequestDto;
import com.codewithmosh.store.dtos.UpdateUserRequest;
import com.codewithmosh.store.dtos.UserDto;
import com.codewithmosh.store.entities.User;
import com.codewithmosh.store.mappers.UserMapper;
import com.codewithmosh.store.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

@RestController
@AllArgsConstructor
@RequestMapping("users")
public class UserControler {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    @GetMapping("")
    public Iterable<UserDto> getAllUsers(@RequestParam(required = false, defaultValue = "", name = "sort") String sortBy){
        Set<String> validNames = new HashSet<>();
        validNames.add("name");
        validNames.add("email");
        if (!validNames.contains(sortBy)) sortBy = "name";
        return userRepository.findAll(Sort.by(sortBy)).stream()
                .map(userMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id){
        User user =  userRepository.findById(id).orElse(null);
        if (user==null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @PostMapping("")
    public ResponseEntity<UserDto> registerUser(@RequestBody RegisterUserRequestDto registerUserRequestDto,
                                                UriComponentsBuilder uriComponentsBuilder){
        User user = userMapper.toEntity(registerUserRequestDto);
        userRepository.save(user);
        System.out.println(user);
        URI uri = uriComponentsBuilder.path("/users/{id}").buildAndExpand(user.getId()).toUri();
        UserDto userDto = userMapper.toDto(user);
        return ResponseEntity.created(uri).body(userDto); //.created returns 201
    }
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable(name = "id") Long id,
                              @RequestBody UpdateUserRequest updateUserRequest){
        User user = userRepository.findById(id).orElse(null);
        if (user==null) return ResponseEntity.notFound().build();
        userMapper.update(updateUserRequest, user);
        userRepository.save(user);
        UserDto userDto = userMapper.toDto(user);

        return ResponseEntity.ok(userDto);
    }

}
