package com.vps.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserProfileRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phone;
}
