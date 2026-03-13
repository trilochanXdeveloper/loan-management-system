package com.loanmanagement.config.oauth2;

import com.loanmanagement.entity.User;
import com.loanmanagement.enums.AuthProvider;
import com.loanmanagement.enums.Role;
import com.loanmanagement.exception.BusinessException;
import com.loanmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // Load user info from Google
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Build user info wrapper
        OAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(
                oAuth2User.getAttributes()
        );

        // Extract email and name
        String email = userInfo.getEmail();
        //String name = userInfo.getName();

        log.info("OAuth2 login attempt: email={}", email);


        userRepository.findByEmail(email).ifPresent(existingUser -> {
                    // If registered locally → block OAuth2 login
                    if (existingUser.getAuthProvider() == AuthProvider.LOCAL) {
                        throw new BusinessException(
                                "Email already registered with password. " +
                                        "Please login with your email and password.",
                                HttpStatus.BAD_REQUEST
                        );
                    }
                }
                // Exists with Google → do nothing, just let them login
        );

        return oAuth2User;
    }
}
