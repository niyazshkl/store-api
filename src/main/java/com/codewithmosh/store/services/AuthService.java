package com.codewithmosh.store.services;

import com.codewithmosh.store.entities.User;
import com.codewithmosh.store.exceptions.auth.UserDoesNotExist;
import com.codewithmosh.store.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public boolean login(String email, String rawPassword){
        User user = userRepository.findByEmail(email).orElse(null);
        if (user==null) throw new UserDoesNotExist();
        String encodedPassword = user.getPassword();
        return passwordEncoder.matches(rawPassword,encodedPassword);
    }
}
