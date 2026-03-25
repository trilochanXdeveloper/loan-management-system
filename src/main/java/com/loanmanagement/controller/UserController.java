package com.loanmanagement.controller;

import com.loanmanagement.dto.request.ChangePasswordRequest;
import com.loanmanagement.dto.request.UpdateCreditScoreRequest;
import com.loanmanagement.dto.request.UpdateProfileRequest;
import com.loanmanagement.dto.response.AddressResponse;
import com.loanmanagement.dto.response.UserResponse;
import com.loanmanagement.dto.response.UserSummaryResponse;
import com.loanmanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ── Profile ──────────────────────────────────────────

    // GET /api/users/profile
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    // PUT /api/users/profile
    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateMyProfile(request));
    }

    // GET /api/users/profile/summary (User Self Only)
    @GetMapping("/profile/summary")
    public ResponseEntity<UserSummaryResponse> getMyProfileSummary() {
        return ResponseEntity.ok(userService.getMyProfileSummary());
    }

    // DELETE /api/users/profile (User Self Only)
    @DeleteMapping("/profile")
    public ResponseEntity<Void> deactivateMyAccount() {
        userService.deactivateMyAccount();
        return ResponseEntity.noContent().build();
    }

    // ── Password ─────────────────────────────────────────

    // POST /api/users/change-password (User Self Only)
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    // ── Address ──────────────────────────────────────────

    // GET /api/users/profile/addresses (User Self Only)
    @GetMapping("/profile/addresses")
    public ResponseEntity<List<AddressResponse>> getMyAddresses() {
        return ResponseEntity.ok(userService.getMyAddresses());
    }

    // DELETE /api/users/profile/addresses/{addressId} (User Self Only)
    @DeleteMapping("/profile/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long addressId) {
        userService.deleteAddress(addressId);
        return ResponseEntity.noContent().build();
    }

    // ── Manager ──────────────────────────────────────────

    // GET /api/users  (MANAGER only)
    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // GET /api/users/search?keyword=value
    @GetMapping("/search")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<UserResponse>> searchUsers(
            @RequestParam String keyword) {
        return ResponseEntity.ok(userService.searchUsers(keyword));
    }

    // GET /api/users/{id}  (MANAGER only)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // PATCH /api/users/{id}/toggle-status  (MANAGER only)
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserResponse> toggleUserStatus(
            @PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleUserStatus(id));
    }

    // PATCH /api/users/{id}/credit-score  (MANAGER only)
    @PatchMapping("/{id}/credit-score")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserResponse> updateCreditScore(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCreditScoreRequest request) {
      return ResponseEntity.ok(
              userService.updateCreditScore(id,request));
    }

    // GET /api/users/{id}/summary  (MANAGER only)
    @GetMapping("/{id}/summary")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserSummaryResponse> getUserSummary(
            @PathVariable Long id){
        return ResponseEntity.ok(userService.getUserSummary(id));
    }

}
