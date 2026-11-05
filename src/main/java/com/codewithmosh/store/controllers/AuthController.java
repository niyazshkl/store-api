package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.auth.JwtResponse;
import com.codewithmosh.store.dtos.auth.RequestLoginDto;
import com.codewithmosh.store.services.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController //Serialize/Deserialization between Java objects and JSON
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

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

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentials(BadCredentialsException exception){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}
