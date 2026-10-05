package com.vps.service;

import com.vps.dto.AuthRequest;
import com.vps.dto.AuthResponse;
import com.vps.dto.RegisterRequest;
import com.vps.entity.User;
import com.vps.repository.UserRepository;
import com.vps.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditLogService auditLogService;

    private static final int MAX_FAILED_ATTEMPTS = 5;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword())); // never store plaintext
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() != null ? request.getRole() : User.Role.CUSTOMER);

        userRepository.save(user);
        auditLogService.log(user, "USER_REGISTERED", "USER", user.getId(),
                "New user registered: " + user.getEmail());

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getFullName(),
                user.getEmail(), user.getRole());
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        // VPS-SR-004: check account lock
        if (user.isLocked()) {
            auditLogService.log(user, "LOGIN_FAILED_LOCKED", "USER", user.getId(),
                    "Login attempt on locked account: " + user.getEmail());
            throw new IllegalStateException("Account is locked. Please contact administrator.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // Increment failed attempts
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            if (user.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.setLocked(true);
                auditLogService.log(user, "ACCOUNT_LOCKED", "USER", user.getId(),
                        "Account locked after " + MAX_FAILED_ATTEMPTS + " failed attempts: " + user.getEmail());
            } else {
                auditLogService.log(user, "LOGIN_FAILED", "USER", user.getId(),
                        "Failed login attempt " + user.getFailedAttempts() + " for: " + user.getEmail());
            }
            userRepository.save(user);
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Successful login — reset failed attempts
        user.setFailedAttempts(0);
        userRepository.save(user);

        auditLogService.log(user, "LOGIN_SUCCESS", "USER", user.getId(),
                "User logged in: " + user.getEmail());

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getFullName(),
                user.getEmail(), user.getRole());
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
