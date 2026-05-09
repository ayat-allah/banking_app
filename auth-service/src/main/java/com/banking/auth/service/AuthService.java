package com.banking.auth.service;

import com.banking.auth.dto.AuthDto;
import com.banking.auth.pattern.observer.AccountEvent;
import com.banking.auth.pattern.observer.AccountEventPublisher;
import com.banking.auth.model.User;
import com.banking.auth.repository.UserRepository;
import com.banking.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // DESIGN PATTERN: Observer — publisher notifies all registered observers
    private final AccountEventPublisher eventPublisher;

    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already registered");
        }

        // Requirement 1.2: password stored hashed
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : User.Role.CUSTOMER)
                .build();

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getId(), saved.getEmail(), saved.getRole().name());

        // Observer Pattern: notify all observers about registration
        eventPublisher.publish(AccountEvent.registered(saved.getId(), saved.getEmail()));

        return AuthDto.AuthResponse.builder()
                .token(token)
                .userId(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .role(saved.getRole().name())
                .message("Registration successful")
                .build();
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // Observer Pattern: notify on wrong password (Requirement 1.6)
            eventPublisher.publish(AccountEvent.loginFailed(request.getEmail(), "Wrong password"));
            throw new RuntimeException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        // Observer Pattern: notify on login success (Requirement 1.6)
        eventPublisher.publish(AccountEvent.loginSuccess(user.getId(), user.getEmail()));

        return AuthDto.AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
    }

    public AuthDto.UserInfoResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return AuthDto.UserInfoResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .frozen(user.isFrozen())
                .active(user.isActive())
                .build();
    }

    public java.util.List<AuthDto.UserInfoResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> AuthDto.UserInfoResponse.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .email(u.getEmail())
                        .phoneNumber(u.getPhoneNumber())
                        .role(u.getRole().name())
                        .frozen(u.isFrozen())
                        .active(u.isActive())
                        .build())
                .toList();
    }

    @Transactional
    public void updateFreezeStatus(String userId, boolean freeze) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setFrozen(freeze);
        userRepository.save(user);
    }
}
