package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.auth.RequestLoginDto;
import com.codewithmosh.store.exceptions.auth.UserDoesNotExist;
import com.codewithmosh.store.services.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController //Serialize/Deserialization between Java objects and JSON
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody RequestLoginDto loginRequest){
        boolean isSuccessful = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
        if (isSuccessful) return ResponseEntity.ok().build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(UserDoesNotExist.class)
    public ResponseEntity<Map<String,String>> handleUserDoesNotExist(UserDoesNotExist exception){
        return ResponseEntity.badRequest().body(
                Map.of("Error", "User does not exist")
        );
    }

}
