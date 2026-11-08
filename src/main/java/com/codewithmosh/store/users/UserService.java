package com.codewithmosh.store.users;

import com.codewithmosh.store.common.UserNotFound;
import com.codewithmosh.store.entities.Role;
import com.codewithmosh.store.auth.EmailAlreadyExists;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashSet;
import java.util.Set;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public Iterable<UserDto> getAllUsers(String sortBy){
        Set<String> validNames = new HashSet<>();
        validNames.add("name");
        validNames.add("email");
        if (!validNames.contains(sortBy)) sortBy = "name";
        return userRepository.findAll(Sort.by(sortBy)).stream()
                .map(userMapper::toDto).toList();
    }

    public UserDto getUser(Long userId) throws UserNotFound{
        User user =  userRepository.findById(userId).orElse(null);
        if (user==null) throw new UserNotFound();
        return userMapper.toDto(user);
    }

    public UserDto registerUser(String name, String email, String password) throws EmailAlreadyExists{
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

    public UserDto updateUser(Long userId, UpdateUserRequest updateUserRequest) throws UserNotFound{
        User user =  userRepository.findById(userId).orElse(null);
        if (user==null) throw new UserNotFound();
        userMapper.update(updateUserRequest, user);
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    public void deleteUser(Long userId) throws UserNotFound {
        User user =  userRepository.findById(userId).orElse(null);
        if (user==null) throw new UserNotFound();
        userRepository.delete(user);
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) throws UserNotFound, OldPasswordMismatchException{
        User user = userRepository.findById(userId).orElse(null);
        if (user==null) throw new UserNotFound();
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) throw new OldPasswordMismatchException();
        user.setPassword(newPassword);
        userRepository.save(user);
    }

}
