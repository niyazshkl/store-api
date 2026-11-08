package com.codewithmosh.store.users;

import com.codewithmosh.store.auth.EmailAlreadyExists;
import com.codewithmosh.store.common.UserNotFound;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class UserControler {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Iterable<UserDto>> getAllUsers(@RequestParam(required = false, defaultValue = "", name = "sort") String sortBy){
        Iterable<UserDto> users = userService.getAllUsers(sortBy);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id){
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PostMapping
    // MethodArgumentNotValidException
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody RegisterUserRequestDto registerUserRequestDto,
                                                UriComponentsBuilder uriComponentsBuilder){
        UserDto userDto = userService.registerUser(registerUserRequestDto.getName(), registerUserRequestDto.getEmail(), registerUserRequestDto.getPassword());
        URI uri = uriComponentsBuilder.path("/users/{id}").buildAndExpand(userDto.getId()).toUri();
        return ResponseEntity.created(uri).body(userDto); //.created returns 201
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable(name = "id") Long id,
                              @RequestBody UpdateUserRequest updateUserRequest){
        return ResponseEntity.ok(userService.updateUser(id, updateUserRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable(name = "id") Long id){
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(@PathVariable Long id,
                                              @Valid @RequestBody ChangePasswordRequestDto changePasswordRequestDto){
        userService.changePassword(id, changePasswordRequestDto.getOldPassword(), changePasswordRequestDto.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(EmailAlreadyExists.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExists(EmailAlreadyExists exception){
        return ResponseEntity.badRequest().body(
                Map.of("email", "Email is already registered")
        );
    }
    @ExceptionHandler(UserNotFound.class)
    public ResponseEntity<Void> handleUserNotFound(){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler(OldPasswordMismatchException.class)
    public ResponseEntity<Map<String,String>> handleOldPasswordMismatch(OldPasswordMismatchException exception){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("Password incorrect", "Old password is incorrect"));
    }

}
