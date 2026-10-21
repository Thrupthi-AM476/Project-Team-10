package com.vps.service;

import com.vps.dto.AuthRequest;
import com.vps.dto.AuthResponse;
import com.vps.dto.RegisterRequest;
import com.vps.dto.UserProfileRequest;
import com.vps.dto.UserProfileResponse;
import com.vps.entity.User;
import com.vps.repository.UserRepository;
import com.vps.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditLogService auditLogService;

    private static final int MAX_FAILED_ATTEMPTS = 5;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword())); // never store plaintext
        user.setPhone(request.getPhone());
        // Always assign CUSTOMER role — callers cannot escalate privilege via registration
        user.setRole(User.Role.CUSTOMER);

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            // Handles the race condition where two concurrent requests both pass the
            // existsByEmail check before either commits (DB unique constraint fires)
            throw new IllegalArgumentException("Email already registered");
        }

        auditLogService.log(user, "USER_REGISTERED", "USER", user.getId(),
                "New user registered: " + user.getEmail());

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getFullName(),
                user.getEmail(), user.getRole());
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        // VPS-SR-004: check account lock
        if (user.isLocked()) {
            auditLogService.log(user, "LOGIN_FAILED_LOCKED", "USER", user.getId(),
                    "Login attempt on locked account: " + user.getEmail());
            throw new IllegalStateException("Account is locked. Please contact administrator.");
        }

        // Reject inactive accounts
        if (!user.isActive()) {
            auditLogService.log(user, "LOGIN_FAILED_INACTIVE", "USER", user.getId(),
                    "Login attempt on inactive account: " + user.getEmail());
            throw new IllegalStateException("Account is inactive. Please contact administrator.");
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

    public UserProfileResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return mapToProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateUserProfile(String email, UserProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Only fullName and phone may be updated — all other fields are protected
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        userRepository.save(user);

        auditLogService.log(user, "PROFILE_UPDATED", "USER", user.getId(),
                "User updated profile: " + user.getEmail());

        return mapToProfileResponse(user);
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setActive(user.isActive());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
