package com.loanmanagement.config.oauth2;

import com.loanmanagement.entity.RefreshToken;
import com.loanmanagement.entity.User;
import com.loanmanagement.enums.AuthProvider;
import com.loanmanagement.enums.Role;
import com.loanmanagement.exception.ResourceNotFoundException;
import com.loanmanagement.repository.RefreshTokenRepository;
import com.loanmanagement.repository.UserRepository;
import com.loanmanagement.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiry}")
    private Long refreshTokenExpiry;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Load user from DB
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .name(name)
                            .email(email)
                            .password(null)
                            .role(Role.CUSTOMER)
                            .authProvider(AuthProvider.GOOGLE)
                            .creditScore(750)
                            .isActive(true)
                            .build();
                    return userRepository.save(newUser);
                });

        // Generate access token
        String accessToken = jwtUtil.generateAccessToken(
                user.getEmail(),
                user.getRole().name()
        );

        // Generate refresh token
        String refreshToken = createRefreshToken(user);

        log.info("OAuth2 login successful: email={}, role={}",
                email, user.getRole());

        // Redirect to frontend with tokens
        String redirectUrl = frontendUrl
                + "/oauth2/callback"
                + "?token=" + accessToken
                + "&refreshToken=" + refreshToken;


        getRedirectStrategy()
                .sendRedirect(request, response, redirectUrl);
    }

    private String createRefreshToken(User user) {

        // Delete old refresh token if exists
        refreshTokenRepository.deleteByUserId(user.getId());
        refreshTokenRepository.flush();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now()
                        .plusSeconds(refreshTokenExpiry / 1000))
                .build();

        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }
}
