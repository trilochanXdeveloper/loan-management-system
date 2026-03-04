package com.loanmanagement.service;

import com.loanmanagement.dto.request.RefreshTokenRequest;
import com.loanmanagement.dto.request.UserLoginRequest;
import com.loanmanagement.dto.request.UserRegisterRequest;
import com.loanmanagement.dto.response.AuthResponse;
import com.loanmanagement.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(UserRegisterRequest request);
    AuthResponse login(UserLoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String token, Long userId);
}
