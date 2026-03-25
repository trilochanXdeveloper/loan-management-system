package com.loanmanagement.service.impl;

import com.loanmanagement.dto.request.RefreshTokenRequest;
import com.loanmanagement.dto.request.UserLoginRequest;
import com.loanmanagement.dto.request.UserRegisterRequest;
import com.loanmanagement.dto.response.AddressResponse;
import com.loanmanagement.dto.response.AuthResponse;
import com.loanmanagement.dto.response.UserResponse;
import com.loanmanagement.entity.RefreshToken;
import com.loanmanagement.entity.User;
import com.loanmanagement.enums.AuthProvider;
import com.loanmanagement.enums.Role;
import com.loanmanagement.exception.BusinessException;
import com.loanmanagement.exception.ResourceNotFoundException;
import com.loanmanagement.repository.AddressRepository;
import com.loanmanagement.repository.RefreshTokenRepository;
import com.loanmanagement.repository.UserRepository;
import com.loanmanagement.service.AuthService;
import com.loanmanagement.service.TokenBlacklistService;
import com.loanmanagement.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh-token-expiry}")
    private Long refreshTokenExpiry;

    // Register Customer
    @Override
    @Transactional
    public UserResponse registerCustomer(UserRegisterRequest request) {
        // Check email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(
                    "Email already registered: " + request.getEmail(),
                    HttpStatus.CONFLICT
            );
        }

        //Build user
        User customer = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .creditScore(750)
                .isActive(true)
                .build();

        return mapToUserResponse(userRepository.save(customer));
    }

    // Register Manager
    @Override
    @Transactional
    public UserResponse registerManager(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(
                    "Email already registered: " + request.getEmail(),
                    HttpStatus.CONFLICT
            );
        }

        User manager = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.MANAGER)
                .authProvider(AuthProvider.LOCAL)
                .creditScore(900)
                .isActive(true)
                .build();

        return mapToUserResponse(userRepository.save(manager));
    }

    // Login
    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public AuthResponse login(UserLoginRequest request) {

        // Find user
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: "
                                + request.getEmail()));

        // Check LOCAL provider or not
        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw new BusinessException(
                    "Please login with "
                            + user.getAuthProvider().name()
                            + " account.",
                    HttpStatus.BAD_REQUEST);
        }

        // Check user active or not
        if (!user.getIsActive()) {
            throw new BusinessException(
                    "Account is deactivated. Please contact support.",
                    HttpStatus.FORBIDDEN);
        }

        // Check account lock
        if (user.getAccountLockedUntil() != null &&
                user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
            long minutesLeft = ChronoUnit.MINUTES.between(
                    LocalDateTime.now(),
                    user.getAccountLockedUntil());
            throw new BusinessException(
                    "Account is locked. Try again in "
                            + minutesLeft + " minutes.",
                    HttpStatus.FORBIDDEN);
        }

        // Manual password check
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);

            if (attempts >= 5) {
                user.setAccountLockedUntil(
                        LocalDateTime.now().plusMinutes(30));
                userRepository.save(user);
                throw new BusinessException(
                        "Too many failed attempts. "
                                + "Account locked for 30 minutes.",
                        HttpStatus.FORBIDDEN);
            }

            userRepository.save(user);
            throw new BusinessException(
                    "Invalid password. "
                            + (5 - attempts) + " attempts left.",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Success -> reset lock fields
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);


        // Generate access token
        String accessToken = jwtUtil.generateAccessToken(
                user.getEmail(),
                user.getRole().name()
        );

        // Generate and save refresh token
        String refreshToken = createRefreshToken(user);


        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(mapToUserResponse(user))
                .build();
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException(
                        "Invalid refresh token",
                        HttpStatus.UNAUTHORIZED
                ));

        // Check if refresh token is expired
        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException(
                    "Refresh token expired. Please login again.",
                    HttpStatus.UNAUTHORIZED
            );
        }

        User user = refreshToken.getUser();

        String newAccessToken = jwtUtil.generateAccessToken(
                user.getEmail(),
                user.getRole().name()
        );

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .user(mapToUserResponse(user))
                .build();
    }

    @Override
    public void logout(String token, Long userId) {
        refreshTokenRepository.deleteByUserId(userId);

        tokenBlacklistService.blacklistToken(token);
    }

    // Helper — Create Refresh Token
    private String createRefreshToken(User user) {

        // Delete existing refresh token first
        refreshTokenRepository.deleteByUserId(user.getId()); // ← add this

        // Flush to ensure delete committed before insert
        refreshTokenRepository.flush(); // ← add this

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now()
                        .plusSeconds(refreshTokenExpiry / 1000))
                .build();

        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

    private UserResponse mapToUserResponse(User user) {
        List<AddressResponse> addresses = user.getAddresses() == null
                ? Collections.emptyList()
                : user.getAddresses()
                   .stream()
                   .map(a -> AddressResponse.builder()
                        .id(a.getId())
                        .street(a.getStreet())
                        .city(a.getCity())
                        .state(a.getState())
                        .pincode(a.getPincode())
                        .addressType(a.getAddressType())
                        .build())
                .toList();

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .dateOfBirth(user.getDateOfBirth())
                .addresses(addresses)
                .role(user.getRole())
                .authProvider(user.getAuthProvider())
                .creditScore(user.getCreditScore())
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .accountLockedUntil(user.getAccountLockedUntil())
                .lastLoginAt(user.getLastLoginAt())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

}
