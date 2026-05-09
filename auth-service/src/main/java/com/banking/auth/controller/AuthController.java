package com.banking.auth.controller;

import com.banking.auth.dto.AuthDto;
import com.banking.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Requirement 1.1: Register with name, phone, password, role
    @PostMapping("/register")
    public ResponseEntity<AuthDto.AuthResponse> register(
            @Valid @RequestBody AuthDto.RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // Requirement 1.3: Login with email/password and receive JWT
    @PostMapping("/login")
    public ResponseEntity<AuthDto.AuthResponse> login(
            @Valid @RequestBody AuthDto.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // Internal endpoint: used by other services to get user info
    @GetMapping("/users/{userId}")
    public ResponseEntity<AuthDto.UserInfoResponse> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(authService.getUserById(userId));
    }

    // Internal endpoint: used by admin-service to freeze/unfreeze
    @PutMapping("/users/{userId}/freeze")
    public ResponseEntity<String> freezeUser(
            @PathVariable String userId,
            @RequestParam boolean freeze) {
        authService.updateFreezeStatus(userId, freeze);
        return ResponseEntity.ok(freeze ? "Account frozen" : "Account unfrozen");
    }

    // Internal endpoint: admin-service lists all users via Feign
    @GetMapping("/internal/users")
    public ResponseEntity<java.util.List<AuthDto.UserInfoResponse>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    // Internal endpoint: admin-service gets user by id via Feign
    @GetMapping("/internal/users/{userId}")
    public ResponseEntity<AuthDto.UserInfoResponse> getUserByIdInternal(@PathVariable String userId) {
        return ResponseEntity.ok(authService.getUserById(userId));
    }

    // Internal endpoint: admin-service freeze/unfreeze via Feign
    @PutMapping("/internal/users/{userId}/freeze")
    public ResponseEntity<String> freezeUserInternal(
            @PathVariable String userId,
            @RequestParam boolean freeze) {
        authService.updateFreezeStatus(userId, freeze);
        return ResponseEntity.ok(freeze ? "Account frozen" : "Account unfrozen");
    }
}
