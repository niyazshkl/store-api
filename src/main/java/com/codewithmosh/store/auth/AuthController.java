package com.codewithmosh.store.auth;

import com.codewithmosh.store.users.UserDto;
import com.codewithmosh.store.users.User;
import com.codewithmosh.store.users.UserMapper;
import com.codewithmosh.store.users.UserRepository;
import com.codewithmosh.store.services.Jwt;
import com.codewithmosh.store.services.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController //Serialize/Deserialization between Java objects and JSON
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtConfig jwtConfig;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody RequestLoginDto loginRequest,
                                             HttpServletResponse response){
        /*
        Authenticate credentials
        If valid, return a token which the user will return in future requests
            for protected (authenticated) endpoints
            This is not session based, so it is stateless and token based authentication
         */

        Authentication authentication = new UsernamePasswordAuthenticationToken
                                        (loginRequest.getEmail(), loginRequest.getPassword());

        authenticationManager.authenticate(authentication); //throws BadCredentialsException at runtime.

        User user = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);
        if (user==null) return ResponseEntity.badRequest().build(); //should never happen because authenticationManager should be throwing and we never get here then

        Jwt accessToken = jwtService.generateAccessToken(user);
        Jwt refershToken = jwtService.generateRefreshToken(user);



        Cookie cookie = new Cookie("refreshToken", refershToken.toString());
        cookie.setHttpOnly(true); //cannot be accessed by js
        cookie.setPath("/auth/refresh"); //where cookie can be sent to
        cookie.setMaxAge(jwtConfig.getRefreshTokenExpirationInSeconds()); //7 days (copied from jwtService::generateRefreshToken)
        cookie.setSecure(true); //https only; http blocked

        response.addCookie(cookie);


        return ResponseEntity.ok(new JwtResponse(accessToken.toString()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshAccessToken(
            @CookieValue(value = "refreshToken") String refreshToken
    ){
        try {
            Jwt jwt = jwtService.parse(refreshToken);
            if (!jwt.isUnexpired()) return ResponseEntity.badRequest().build();
            Long userId = Long.valueOf(jwt.getSubject().toString());
            User user = userRepository.findById(userId).orElseThrow(); //maybe user was deleted from the table in the schema
            Jwt refreshedAccessToken = jwtService.generateAccessToken(user);
            return ResponseEntity.ok(new JwtResponse(refreshedAccessToken.toString()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Void> validateJwt(@RequestHeader(name = "Authorization") String token){
        token = token.replace("Bearer ", "");
        Jwt jwt = null;
        try {
            jwt = jwtService.parse(token);
            boolean valid = jwt.isUnexpired();
            if (valid) return ResponseEntity.ok().build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Long userId = Long.valueOf(authentication.getPrincipal().toString()); //we know because we set this

        User user = userRepository.findById(userId).orElse(null);

        //if user not found -> someone got a valid jwtToken without being registered in the users table in store_api schema
        if (user==null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentials(BadCredentialsException exception){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}
