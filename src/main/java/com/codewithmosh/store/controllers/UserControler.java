package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.users.ChangePasswordRequestDto;
import com.codewithmosh.store.dtos.users.RegisterUserRequestDto;
import com.codewithmosh.store.dtos.users.UpdateUserRequest;
import com.codewithmosh.store.dtos.users.UserDto;
import com.codewithmosh.store.entities.User;
import com.codewithmosh.store.mappers.UserMapper;
import com.codewithmosh.store.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
    // MethodArgumentNotValidException
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody RegisterUserRequestDto registerUserRequestDto,
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable(name = "id") Long id){
        User user = userRepository.findById(id).orElse(null);
        if (user==null) return ResponseEntity.notFound().build();
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(@PathVariable Long id,
                                               @RequestBody ChangePasswordRequestDto changePasswordRequestDto){
        User user = userRepository.findById(id).orElse(null);
        if (user==null) return ResponseEntity.notFound().build();
        if (!user.getPassword().equals(changePasswordRequestDto.getOldPassword())) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        user.setPassword(changePasswordRequestDto.getNewPassword());
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

}
