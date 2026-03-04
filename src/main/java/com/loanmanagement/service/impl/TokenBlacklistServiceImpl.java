package com.loanmanagement.service.impl;

import com.loanmanagement.entity.TokenBlacklist;
import com.loanmanagement.repository.TokenBlacklistRepository;
import com.loanmanagement.service.TokenBlacklistService;
import com.loanmanagement.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final JwtUtil jwtUtil;

    // Add Token to Blacklist
    @Override
    @Transactional
    public void blacklistToken(String token) {
        // Only if token is not already blacklisted
        if (!tokenBlacklistRepository.existsByToken(token)){
            TokenBlacklist blacklistedToken = TokenBlacklist.builder()
                    .token(token)
                    .expiryDate(jwtUtil.extractExpiration(token))
                    .build();
            tokenBlacklistRepository.save(blacklistedToken);
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupExpiredTokens() {

    }
}
