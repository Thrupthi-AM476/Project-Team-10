package com.vps.controller;

import com.vps.dto.ApiResponse;
import com.vps.dto.UserProfileRequest;
import com.vps.dto.UserProfileResponse;
import com.vps.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    // GET /api/v1/users/me — retrieve the authenticated user's own profile
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal UserDetails principal) {
        String email = principal.getUsername();
        UserProfileResponse response = authService.getUserProfile(email);
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved", response));
    }

    // PUT /api/v1/users/me — update only permitted fields (fullName, phone)
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UserProfileRequest request) {
        String email = principal.getUsername();
        UserProfileResponse response = authService.updateUserProfile(email, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", response));
    }
}
