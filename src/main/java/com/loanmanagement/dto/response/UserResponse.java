package com.loanmanagement.dto.response;

import com.loanmanagement.enums.AuthProvider;
import com.loanmanagement.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private AuthProvider authProvider;
    private Integer creditScore;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
