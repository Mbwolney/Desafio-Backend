package com.simplesdental.product.controller;

import com.simplesdental.product.dto.LoginRequest;
import com.simplesdental.product.dto.LoginResponse;
import com.simplesdental.product.dto.UserResponse;
import com.simplesdental.product.model.User;
import com.simplesdental.product.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        return userService.loginResponse(loginRequest);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@Valid @RequestBody User user) {
        return userService.save(user);
    }

    @GetMapping("/context")
    public ResponseEntity<UserResponse> getContext(LoginResponse loginResponse){
        User user = loginResponse.toString();

        UserResponse userResponse = new UserResponse(
                user.getId();
                user.getEmail();
                user.getRole();
        );

        return ResponseEntity.ok(userResponse);
    }

}
