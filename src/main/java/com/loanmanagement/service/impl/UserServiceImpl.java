package com.loanmanagement.service.impl;

import com.loanmanagement.dto.request.AddressRequest;
import com.loanmanagement.dto.request.ChangePasswordRequest;
import com.loanmanagement.dto.request.UpdateCreditScoreRequest;
import com.loanmanagement.dto.request.UpdateProfileRequest;
import com.loanmanagement.dto.response.AddressResponse;
import com.loanmanagement.dto.response.UserResponse;
import com.loanmanagement.dto.response.UserSummaryResponse;
import com.loanmanagement.entity.Address;
import com.loanmanagement.entity.Loan;
import com.loanmanagement.entity.User;
import com.loanmanagement.enums.AuthProvider;
import com.loanmanagement.enums.LoanStatus;
import com.loanmanagement.exception.BusinessException;
import com.loanmanagement.exception.ResourceNotFoundException;
import com.loanmanagement.repository.AddressRepository;
import com.loanmanagement.repository.LoanRepository;
import com.loanmanagement.repository.UserRepository;
import com.loanmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final LoanRepository loanRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    // ========================
    // Get My Profile
    // ========================
    @Transactional(readOnly = true)
    @Override
    public UserResponse getMyProfile() {
        User user = getCurrentUser();
        log.info("Profile fetched: email={}", user.getEmail());
        return mapToUserResponse(user);
    }

    // ========================
    // Update My Profile
    // ========================
    @Override
    @Transactional
    public UserResponse updateMyProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();

        // Update basic fields
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setDateOfBirth(request.getDateOfBirth());
        userRepository.save(user);

        // Update addresses if provided
        if (request.getAddresses() != null) {
            for (AddressRequest addressRequest : request.getAddresses()) {
                // Find existing or create new
                Address address = addressRepository
                        .findByUserIdAndAddressType(
                                user.getId(),
                                addressRequest.getAddressType())
                        .orElse(Address.builder()
                                .user(user)
                                .addressType(addressRequest.getAddressType())
                                .build());

                address.setStreet(addressRequest.getStreet());
                address.setCity(addressRequest.getCity());
                address.setState(addressRequest.getState());
                address.setPincode(addressRequest.getPincode());

                addressRepository.save(address);
            }
        }
        log.info("Profile updated: email={}", user.getEmail());
        return mapToUserResponse(user);
    }

    // ========================
    // Get My Profile Summary
    // ========================
    @Override
    @Transactional(readOnly = true)
    public UserSummaryResponse getMyProfileSummary() {
        User user = getCurrentUser();
        return buildSummary(user);
    }

    // ========================
    // Deactivate My Account
    // ========================
    @Override
    @Transactional
    public void deactivateMyAccount() {
        User user = getCurrentUser();

        long activeLoans = loanRepository
                .countByUserIdAndStatus(user.getId(), LoanStatus.APPROVED);

        if (activeLoans > 0) {
            throw new BusinessException(
                    "Cannot deactivate account with active loans. "
                            + "Please close all loans first.",
                    HttpStatus.BAD_REQUEST);
        }

        user.setIsActive(false);
        userRepository.save(user);
        log.info("Account deactivated: email={}", user.getEmail());

    }

    // ========================
    // Change Password
    // ========================
    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();

        // OAuth2 users cannot change password
        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw new BusinessException(
                    "Password change is not available for "
                            + user.getAuthProvider() + " accounts.",
                    HttpStatus.BAD_REQUEST);
        }

        // Verify current password
        if (!passwordEncoder.matches(
                request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(
                    "Current password is incorrect.",
                    HttpStatus.BAD_REQUEST);
        }

        // Confirm passwords match
        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {
            throw new BusinessException(
                    "New password and confirm password do not match.",
                    HttpStatus.BAD_REQUEST);
        }

        // Cannot reuse current password
        if (passwordEncoder.matches(
                request.getNewPassword(), user.getPassword())) {
            throw new BusinessException(
                    "New password cannot be same as current password.",
                    HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed: email={}", user.getEmail());
    }

    // ========================
    // Get My Addresses
    // ========================
    @Override
    public List<AddressResponse> getMyAddresses() {
        User user = getCurrentUser();

        return addressRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToAddressResponse)
                .toList();
    }

    // ========================
    // Delete Address (USER)
    // ========================
    @Override
    @Transactional
    public void deleteAddress(Long addressId) {
        User user = getCurrentUser();

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Address not found with id: " + addressId));

        // Can only delete own address
        if (!address.getUser().getId().equals(user.getId())) {
            throw new BusinessException(
                    "You are not authorized to delete this address.",
                    HttpStatus.FORBIDDEN);
        }

        addressRepository.delete(address);
        log.info("Address deleted: addressId={}, userId={}",
                addressId, user.getId());
    }

    // ========================
    // Get All Users (Manager)
    // ========================
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    // ========================
    // Search Users (Manager)
    // ========================
    @Override
    public List<UserResponse> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        log.info("Searching users: keyword={}", keyword);
        return userRepository
                .searchByNameOrEmail(keyword.trim())
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    // ========================
    // Get User By ID (Manager)
    // ========================
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));
        return mapToUserResponse(user);
    }

    // ========================
    // Toggle User Status
    // ========================
    @Override
    public UserResponse toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));

        user.setIsActive(!user.getIsActive());
        User updated = userRepository.save(user);

        log.info("User status toggled: email={}, isActive={}",
                user.getEmail(), updated.getIsActive());

        return mapToUserResponse(user);
    }

    // ========================
    // Update Credit Score
    // ========================
    @Override
    @Transactional
    public UserResponse updateCreditScore(
            Long id, UpdateCreditScoreRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));

        int oldScore = user.getCreditScore();
        user.setCreditScore(request.getCreditScore());
        User updatedUser = userRepository.save(user);

        log.info("Credit score updated: userId={}, {} → {}, reason={}",
                id, oldScore,
                request.getCreditScore(), request.getReason());

        return mapToUserResponse(updatedUser);
    }


    // ========================
    // Get User Summary (Manager)
    // ========================
    @Override
    @Transactional(readOnly = true)
    public UserSummaryResponse getUserSummary(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));
        return buildSummary(user);
    }

    // ========================
    // Helper — Get Current User
    // ========================
    private User getCurrentUser() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + email));
    }

    // ========================
    // Helper — Build Summary
    // ========================
    private UserSummaryResponse buildSummary(User user) {
        List<Loan> loans = loanRepository
                .findByUserId(user.getId());

        long activeLoans = loans.stream()
                .filter(loan -> loan.getStatus() == LoanStatus.APPROVED)
                .count();

        long pendingLoans = loans.stream()
                .filter(loan -> loan.getStatus() == LoanStatus.PENDING)
                .count();

        long rejectedLoans = loans.stream()
                .filter(loan -> loan.getStatus() == LoanStatus.REJECTED)
                .count();

        BigDecimal totalAmount = loans.stream()
                .filter(loan -> loan.getStatus() == LoanStatus.APPROVED)
                .map(Loan::getLoanAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean hasAddress = !addressRepository
                .findByUserId(user.getId()).isEmpty();

        boolean kycComplete = user.getPhone() != null
                && user.getDateOfBirth() != null
                && hasAddress;

        return UserSummaryResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .creditScore(user.getCreditScore())
                .totalLoansApplied((long) loans.size())
                .activeLoans(activeLoans)
                .pendingLoans(pendingLoans)
                .rejectedLoans(rejectedLoans)
                .totalAmountBorrowed(totalAmount)
                .kycComplete(kycComplete)
                .hasPhone(user.getPhone() != null)
                .hasDob(user.getDateOfBirth() != null)
                .hasAddress(hasAddress)
                .build();
    }

    // ========================
    // Helper — Map Address
    // ========================
    private AddressResponse mapToAddressResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .addressType(address.getAddressType())
                .build();
    }

    // ========================
    // Helper — Map to Response
    // ========================
    private UserResponse mapToUserResponse(User user) {
        List<AddressResponse> addresses =
                user.getId() != null
                        ? addressRepository.findByUserId(user.getId())
                        .stream()
                        .map(this::mapToAddressResponse)
                        .toList()
                        : Collections.emptyList();

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .authProvider(user.getAuthProvider())
                .creditScore(user.getCreditScore())
                .isActive(user.getIsActive())
                .phone(user.getPhone())
                .dateOfBirth(user.getDateOfBirth())
                .addresses(addresses)
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .accountLockedUntil(user.getAccountLockedUntil())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
