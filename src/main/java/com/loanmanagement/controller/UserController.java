package com.loanmanagement.controller;

import com.loanmanagement.dto.request.ChangePasswordRequest;
import com.loanmanagement.dto.request.UpdateCreditScoreRequest;
import com.loanmanagement.dto.request.UpdateProfileRequest;
import com.loanmanagement.dto.response.AddressResponse;
import com.loanmanagement.dto.response.UserResponse;
import com.loanmanagement.dto.response.UserSummaryResponse;
import com.loanmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
@Tag(name = "User Management",
        description = "Profile, Address, Password, Manager operations")
public class UserController {

    private final UserService userService;

    // ── Profile ──────────────────────────────────────────

    // GET /api/users/profile
    @Operation(summary = "Get my profile")
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    // PUT /api/users/profile
    @Operation(summary = "Update my profile — name, phone, DOB, addresses")
    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateMyProfile(request));
    }

    // GET /api/users/profile/summary (User Self Only)
    @Operation(summary = "Get my profile summary — loan stats + KYC status")
    @GetMapping("/profile/summary")
    public ResponseEntity<UserSummaryResponse> getMyProfileSummary() {
        return ResponseEntity.ok(userService.getMyProfileSummary());
    }

    // DELETE /api/users/profile (User Self Only)
    @Operation(summary = "Deactivate my account")
    @DeleteMapping("/profile")
    public ResponseEntity<Void> deactivateMyAccount() {
        userService.deactivateMyAccount();
        return ResponseEntity.noContent().build();
    }

    // ── Password ─────────────────────────────────────────

    // POST /api/users/change-password (User Self Only)
    @Operation(summary = "Change password — LOCAL users only")
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    // ── Address ──────────────────────────────────────────

    // GET /api/users/profile/addresses (User Self Only)
    @Operation(summary = "Get my addresses")
    @GetMapping("/profile/addresses")
    public ResponseEntity<List<AddressResponse>> getMyAddresses() {
        return ResponseEntity.ok(userService.getMyAddresses());
    }

    // DELETE /api/users/profile/addresses/{addressId} (User Self Only)
    @Operation(summary = "Delete my address by ID")
    @DeleteMapping("/profile/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long addressId) {
        userService.deleteAddress(addressId);
        return ResponseEntity.noContent().build();
    }

    // ── Manager ──────────────────────────────────────────

    // GET /api/users  (MANAGER only)
    @Operation(summary = "Get all users — MANAGER only")
    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // GET /api/users/search?keyword=value
    @Operation(summary = "Search users by name or email — MANAGER only")
    @GetMapping("/search")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<UserResponse>> searchUsers(
            @RequestParam String keyword) {
        return ResponseEntity.ok(userService.searchUsers(keyword));
    }

    // GET /api/users/{id}  (MANAGER only)
    @Operation(summary = "Get user by ID — MANAGER only")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // PATCH /api/users/{id}/toggle-status  (MANAGER only)
    @Operation(summary = "Toggle user active status — MANAGER only")
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserResponse> toggleUserStatus(
            @PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleUserStatus(id));
    }

    // PATCH /api/users/{id}/credit-score  (MANAGER only)
    @Operation(summary = "Update credit score — MANAGER only")
    @PatchMapping("/{id}/credit-score")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserResponse> updateCreditScore(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCreditScoreRequest request) {
      return ResponseEntity.ok(
              userService.updateCreditScore(id,request));
    }

    // GET /api/users/{id}/summary  (MANAGER only)
    @Operation(summary = "Get user loan summary — MANAGER only")
    @GetMapping("/{id}/summary")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserSummaryResponse> getUserSummary(
            @PathVariable Long id){
        return ResponseEntity.ok(userService.getUserSummary(id));
    }

}
