package com.vps.service;

import com.vps.dto.AuthRequest;
import com.vps.dto.AuthResponse;
import com.vps.dto.RegisterRequest;
import com.vps.entity.User;
import com.vps.repository.UserRepository;
import com.vps.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("Alice Smith");
        registerRequest.setEmail("alice@example.com");
        registerRequest.setPassword("Password1!");
        registerRequest.setPhone("1234567890");

        authRequest = new AuthRequest();
        authRequest.setEmail("alice@example.com");
        authRequest.setPassword("Password1!");
    }

    // (1) Successful registration
    @Test
    void register_success() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1!")).thenReturn("$2a$hashed");
        when(jwtUtil.generateToken("alice@example.com", "CUSTOMER")).thenReturn("jwt-token");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setFullName("Alice Smith");
        savedUser.setEmail("alice@example.com");
        savedUser.setPasswordHash("$2a$hashed");
        savedUser.setRole(User.Role.CUSTOMER);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AuthResponse response = authService.register(registerRequest);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo(User.Role.CUSTOMER);
        // No passwordHash in AuthResponse
        assertThat(response).doesNotHaveToString("passwordHash");
    }

    // (2) Duplicate email registration
    @Test
    void register_duplicateEmail() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already registered");
    }

    // (3) Role cannot be elevated — role field is absent from RegisterRequest
    @Test
    void register_roleCannotBeElevated() {
        // RegisterRequest has no role field; we verify the saved user always gets CUSTOMER
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$hashed");
        when(jwtUtil.generateToken(anyString(), eq("CUSTOMER"))).thenReturn("jwt-token");

        // Capture the user that gets saved
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        authService.register(registerRequest);

        verify(userRepository).save(argThat(u -> u.getRole() == User.Role.CUSTOMER));
    }

    // (4) Concurrent duplicate registration — DB throws DataIntegrityViolationException
    @Test
    void register_concurrentDuplicate() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$hashed");
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already registered");
    }

    // (5) Successful login
    @Test
    void login_success() {
        User user = new User();
        user.setId(1L);
        user.setEmail("alice@example.com");
        user.setFullName("Alice Smith");
        user.setPasswordHash("$2a$hashed");
        user.setRole(User.Role.CUSTOMER);
        user.setLocked(false);
        user.setActive(true);
        user.setFailedAttempts(0);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1!", "$2a$hashed")).thenReturn(true);
        when(jwtUtil.generateToken("alice@example.com", "CUSTOMER")).thenReturn("jwt-token");
        when(userRepository.save(any(User.class))).thenReturn(user);

        AuthResponse response = authService.login(authRequest);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(user.getFailedAttempts()).isEqualTo(0);
    }

    // (6) Login with invalid password
    @Test
    void login_invalidPassword() {
        User user = new User();
        user.setId(1L);
        user.setEmail("alice@example.com");
        user.setPasswordHash("$2a$hashed");
        user.setRole(User.Role.CUSTOMER);
        user.setLocked(false);
        user.setActive(true);
        user.setFailedAttempts(0);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1!", "$2a$hashed")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        assertThatThrownBy(() -> authService.login(authRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email or password");

        assertThat(user.getFailedAttempts()).isEqualTo(1);
    }

    // (7) Login on locked account
    @Test
    void login_accountLocked() {
        User user = new User();
        user.setId(1L);
        user.setEmail("alice@example.com");
        user.setPasswordHash("$2a$hashed");
        user.setRole(User.Role.CUSTOMER);
        user.setLocked(true);
        user.setActive(true);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(authRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("locked");
    }

    // (8) Login on inactive account
    @Test
    void login_inactiveAccount() {
        User user = new User();
        user.setId(1L);
        user.setEmail("alice@example.com");
        user.setPasswordHash("$2a$hashed");
        user.setRole(User.Role.CUSTOMER);
        user.setLocked(false);
        user.setActive(false);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(authRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("inactive");
    }

    // (9) Account locks after five consecutive failed attempts
    @Test
    void login_lockAfterFiveFailures() {
        User user = new User();
        user.setId(1L);
        user.setEmail("alice@example.com");
        user.setPasswordHash("$2a$hashed");
        user.setRole(User.Role.CUSTOMER);
        user.setLocked(false);
        user.setActive(true);
        user.setFailedAttempts(4); // 4 prior failures

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1!", "$2a$hashed")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        assertThatThrownBy(() -> authService.login(authRequest))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(user.isLocked()).isTrue();
        assertThat(user.getFailedAttempts()).isEqualTo(5);
    }
}
