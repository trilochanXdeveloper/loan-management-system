package com.loanmanagement.service;

import com.loanmanagement.dto.request.ChangePasswordRequest;
import com.loanmanagement.dto.request.UpdateCreditScoreRequest;
import com.loanmanagement.dto.request.UpdateProfileRequest;
import com.loanmanagement.dto.response.AddressResponse;
import com.loanmanagement.dto.response.UserResponse;
import com.loanmanagement.dto.response.UserSummaryResponse;
import com.loanmanagement.entity.User;

import java.util.List;

public interface UserService {

    // Profile
    UserResponse getMyProfile();
    UserResponse updateMyProfile(UpdateProfileRequest request);
    UserSummaryResponse getMyProfileSummary();
    void deactivateMyAccount();

    // Password
    void changePassword(ChangePasswordRequest request);

    // Address
    List<AddressResponse> getMyAddresses();
    void deleteAddress(Long addressId);

    // Manager
    List<UserResponse> getAllUsers();
    List<UserResponse> searchUsers(String keyword);
    UserResponse getUserById(Long id);
    UserResponse toggleUserStatus(Long id);
    UserResponse updateCreditScore(Long id, UpdateCreditScoreRequest request);
    UserSummaryResponse getUserSummary(Long id);



}