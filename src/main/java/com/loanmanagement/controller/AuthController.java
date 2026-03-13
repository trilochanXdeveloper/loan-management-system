package com.loanmanagement.controller;

import com.loanmanagement.dto.request.RefreshTokenRequest;
import com.loanmanagement.dto.request.UserLoginRequest;
import com.loanmanagement.dto.request.UserRegisterRequest;
import com.loanmanagement.dto.response.AuthResponse;
import com.loanmanagement.dto.response.UserResponse;
import com.loanmanagement.entity.User;
import com.loanmanagement.exception.ResourceNotFoundException;
import com.loanmanagement.repository.UserRepository;
import com.loanmanagement.service.AuthService;
import com.loanmanagement.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    // Customer Register
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody UserRegisterRequest request) {

        return new ResponseEntity<>(
                authService.registerCustomer(request),
                HttpStatus.CREATED
        );
    }
    // Manager Register
    // Only existing MANAGER can create new manager
    @PostMapping("/register/manager")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserResponse> registerManager(
            @Valid @RequestBody UserRegisterRequest request
    ){
        return new ResponseEntity<>(
                authService.registerManager(request),
                HttpStatus.CREATED
        );
    }

    // Login
    @PostMapping("login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody UserLoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // Refresh Token
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request){
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    // Logout
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request){

        // Extract token from header
        String authHeader = request.getHeader("Authorization");
        String token = authHeader.substring(7);

        // Get user id from token
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"));

        authService.logout(token, user.getId());
        return ResponseEntity.ok("Logged out successfully");
    }
}
