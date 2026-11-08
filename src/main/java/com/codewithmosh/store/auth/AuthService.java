package com.codewithmosh.store.auth;

import com.codewithmosh.store.common.UserNotFound;
import com.codewithmosh.store.services.Jwt;
import com.codewithmosh.store.services.JwtService;
import com.codewithmosh.store.users.User;
import com.codewithmosh.store.users.UserDto;
import com.codewithmosh.store.users.UserMapper;
import com.codewithmosh.store.users.UserRepository;
import io.jsonwebtoken.JwtException;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor

public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public LoginResponse handleLogin(String email, String rawPassword){
        Authentication authentication = new UsernamePasswordAuthenticationToken
                (email, rawPassword);

        authenticationManager.authenticate(authentication); //throws BadCredentialsException at runtime.

        User user = userRepository.findByEmail(email).orElse(null);
//        if (user==null) return ResponseEntity.badRequest().build(); //should never happen because authenticationManager should be throwing and we never get here then

        Jwt accessToken = jwtService.generateAccessToken(user);
        Jwt refreshToken = jwtService.generateRefreshToken(user);
        return new LoginResponse(accessToken, refreshToken);
    }

    public Jwt handleRefreshAccessToken(String refreshToken) throws JwtException{
        try {
            Jwt jwt = jwtService.parse(refreshToken);
            if (!jwt.isUnexpired()) throw new JwtException("Expired refresh token!");
            Long userId = Long.valueOf(jwt.getSubject().toString());
            User user = userRepository.findById(userId).orElseThrow(); //maybe user was deleted from the table in the schema
            return jwtService.generateAccessToken(user);
        } catch (Exception e) {
            throw new JwtException("Invalid refresh token!");
        }
    }

    public boolean handleValidJwt(String token) throws JwtException{
        try {
            Jwt jwt = jwtService.parse(token);
            return jwt.isUnexpired();
        } catch (Exception e) {
            throw new JwtException("Invalid token!");
        }
    }

    public UserDto handleMe() throws UserNotFound {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Long userId = Long.valueOf(authentication.getPrincipal().toString()); //we know because we set this

        User user = userRepository.findById(userId).orElse(null);

        //if user not found -> someone got a valid jwtToken without being registered in the users table in store_api schema
        if (user==null) throw new UserNotFound();
        return userMapper.toDto(user);
    }
}
