package com.loanmanagement.config;

import com.loanmanagement.entity.User;
import com.loanmanagement.exception.ResourceNotFoundException;
import com.loanmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).
                orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + email));
        return new CustomUserDetails(user);
    }
}
