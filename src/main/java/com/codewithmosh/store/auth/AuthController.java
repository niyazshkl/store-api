package com.codewithmosh.store.auth;

import com.codewithmosh.store.common.UserNotFound;
import com.codewithmosh.store.users.UserDto;
import com.codewithmosh.store.users.UserMapper;
import com.codewithmosh.store.users.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController //Serialize/Deserialization between Java objects and JSON
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
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
        LoginResponse loginResponse = authService.handleLogin(loginRequest.getEmail(), loginRequest.getPassword());
        Jwt accessToken = loginResponse.getAccessToken();
        Jwt refreshToken = loginResponse.getRefreshToken();

        Cookie cookie = new Cookie("refreshToken", refreshToken.toString());
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
        Jwt refreshedAccessToken = authService.handleRefreshAccessToken(refreshToken);
        return ResponseEntity.ok(new JwtResponse(refreshedAccessToken.toString()));
    }

    @PostMapping("/validate")
    public ResponseEntity<Void> validateJwt(@RequestHeader(name = "Authorization") String authHeader){
        String token = authHeader.replace("Bearer ", "");
        if (authService.handleValidJwt(token)) return ResponseEntity.ok().build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(){
        UserDto userDto = authService.handleMe();
        return ResponseEntity.ok(userDto);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentials(BadCredentialsException exception){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String,String>> handleJwtException(JwtException exception){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("Error", exception.getMessage()));
    }

    @ExceptionHandler(UserNotFound.class)
    public ResponseEntity<Void> handleUserNotFound(){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}
