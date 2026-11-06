package com.codewithmosh.store.services;

import com.codewithmosh.store.dtos.users.UserDto;
import com.codewithmosh.store.entities.Role;
import com.codewithmosh.store.entities.User;
import com.codewithmosh.store.exceptions.auth.EmailAlreadyExists;
import com.codewithmosh.store.mappers.UserMapper;
import com.codewithmosh.store.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserDto registerUser(String name, String email, String password){
        if (userRepository.existsByEmail(email)) throw new EmailAlreadyExists();
//        User user = userMapper.toEntity(registerUserRequestDto);
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.USER);
        userRepository.save(user);
        System.out.println(user);
        UserDto userDto = userMapper.toDto(user);
        return userDto;
    }
}
