package com.loanmanagement.service;

public interface TokenBlacklistService {
    void blacklistToken(String token);
    boolean isBlacklisted(String token);
    void cleanupExpiredTokens();
}
