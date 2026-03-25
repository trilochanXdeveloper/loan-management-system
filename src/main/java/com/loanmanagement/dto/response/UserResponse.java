package com.loanmanagement.dto.response;

import com.loanmanagement.enums.AuthProvider;
import com.loanmanagement.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private List<AddressResponse> addresses;
    private Role role;
    private AuthProvider authProvider;
    private Integer creditScore;

    private int failedLoginAttempts;
    private LocalDateTime accountLockedUntil;
    private LocalDateTime lastLoginAt;

    private Boolean isActive;
    private LocalDateTime createdAt;

}
