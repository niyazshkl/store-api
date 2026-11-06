package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.auth.JwtResponse;
import com.codewithmosh.store.dtos.auth.RequestLoginDto;
import com.codewithmosh.store.dtos.users.UserDto;
import com.codewithmosh.store.entities.User;
import com.codewithmosh.store.mappers.UserMapper;
import com.codewithmosh.store.repositories.UserRepository;
import com.codewithmosh.store.services.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController //Serialize/Deserialization between Java objects and JSON
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody RequestLoginDto loginRequest){
        /*
        Authenticate credentials
        If valid, return a token which the user will return in future requests
            for protected (authenticated) endpoints
            This is not session based, so it is stateless and token based authentication
         */

        Authentication authentication = new UsernamePasswordAuthenticationToken
                                        (loginRequest.getEmail(), loginRequest.getPassword());

        authenticationManager.authenticate(authentication); //throws BadCredentialsException at runtime.
        String token = jwtService.generateToken(loginRequest.getEmail());
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping("/validate")
    public ResponseEntity<Void> validateJwt(@RequestHeader(name = "Authorization") String token){
        token = token.replace("Bearer ", "");
        boolean valid =jwtService.validateToken(token);
        if (valid) return ResponseEntity.ok().build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = (String) authentication.getPrincipal(); //we know because we set this

        User user = userRepository.findByEmail(email).orElse(null);

        //if user not found -> someone got a valid jwtToken without being registered in the users table in store_api schema
        if (user==null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentials(BadCredentialsException exception){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}
