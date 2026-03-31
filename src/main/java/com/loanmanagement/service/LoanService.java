package com.loanmanagement.service;

import com.loanmanagement.dto.request.CollateralRequest;
import com.loanmanagement.dto.request.LoanRequest;
import com.loanmanagement.dto.response.LoanEligibilityResponse;
import com.loanmanagement.dto.response.LoanResponse;
import com.loanmanagement.dto.response.LoanStatsResponse;

import java.time.LocalDate;
import java.util.List;

public interface LoanService {

    // Customer
    LoanResponse applyLoan(LoanRequest request);

    LoanEligibilityResponse checkEligibility(LoanRequest request);

    List<LoanResponse> getMyLoans();

    LoanResponse getLoanById(Long id);

    LoanResponse submitCollateral(
            Long id, CollateralRequest request);

    LoanResponse cancelLoan(Long id);

    // Manager
    List<LoanResponse> getAllLoans(
            String status, String loanType,
            LocalDate from, LocalDate to);

    LoanResponse updateLoanStatus(Long id, String status);

    LoanStatsResponse getLoanStats();

}
