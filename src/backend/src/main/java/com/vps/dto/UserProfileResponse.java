package com.vps.dto;

import com.vps.entity.User.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private boolean isActive;
    private LocalDateTime createdAt;
}
