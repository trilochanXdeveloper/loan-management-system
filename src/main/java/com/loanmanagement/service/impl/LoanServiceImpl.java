package com.loanmanagement.service.impl;

import com.loanmanagement.dto.request.CollateralRequest;
import com.loanmanagement.dto.request.LoanRequest;
import com.loanmanagement.dto.response.LoanEligibilityResponse;
import com.loanmanagement.dto.response.LoanResponse;
import com.loanmanagement.dto.response.LoanStatsResponse;
import com.loanmanagement.entity.Loan;
import com.loanmanagement.entity.User;
import com.loanmanagement.enums.LoanStatus;
import com.loanmanagement.enums.LoanType;
import com.loanmanagement.enums.Role;
import com.loanmanagement.exception.BusinessException;
import com.loanmanagement.exception.ResourceNotFoundException;
import com.loanmanagement.repository.LoanRepository;
import com.loanmanagement.repository.UserRepository;
import com.loanmanagement.service.LoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    // ── Credit Score Minimums ──────────────────────────────
    private static final Map<LoanType, Integer>
            MIN_CREDIT_SCORE = Map.of(
            LoanType.HOME_LOAN, 700,
            LoanType.PERSONAL_LOAN, 650,
            LoanType.EDUCATION_LOAN, 600,
            LoanType.CAR_LOAN, 650,
            LoanType.GOLD_LOAN, 600,
            LoanType.BUSINESS_LOAN, 700
    );

    // ── Interest Rates ─────────────────────────────────────
    private static final Map<LoanType, BigDecimal>
            INTEREST_RATES = Map.of(
            LoanType.HOME_LOAN, new BigDecimal("8.2"),
            LoanType.PERSONAL_LOAN, new BigDecimal("14.00"),
            LoanType.EDUCATION_LOAN, new BigDecimal("9.00"),
            LoanType.CAR_LOAN, new BigDecimal("10.00"),
            LoanType.GOLD_LOAN, new BigDecimal("11.00"),
            LoanType.BUSINESS_LOAN, new BigDecimal("12.00")
    );

    // ── Processing Fee % ───────────────────────────────────
    private static final Map<LoanType, BigDecimal>
            PROCESSING_FEE_PERCENT = Map.of(
            LoanType.HOME_LOAN, new BigDecimal("0.50"),
            LoanType.PERSONAL_LOAN, new BigDecimal("2.00"),
            LoanType.EDUCATION_LOAN, new BigDecimal("1.00"),
            LoanType.CAR_LOAN, new BigDecimal("1.00"),
            LoanType.GOLD_LOAN, new BigDecimal("0.50"),
            LoanType.BUSINESS_LOAN, new BigDecimal("1.50")
    );

    // ── Base Max Amounts ───────────────────────────────────
    private static final Map<LoanType, BigDecimal>
            BASE_MAX_AMOUNTS = Map.of(
            LoanType.HOME_LOAN, new BigDecimal("10000000"),
            LoanType.PERSONAL_LOAN, new BigDecimal("500000"),
            LoanType.EDUCATION_LOAN, new BigDecimal("2000000"),
            LoanType.CAR_LOAN, new BigDecimal("1500000"),
            LoanType.GOLD_LOAN, new BigDecimal("500000"),
            LoanType.BUSINESS_LOAN, new BigDecimal("5000000")
    );

    private static final int MAX_ACTIVE_LOANS = 2;

    // ========================
    // Apply for Loan
    // ========================
    @Override
    @Transactional
    public LoanResponse applyLoan(LoanRequest request) {
        User user = getCurrentUser();

        // Step 1 — KYC check
        validateKyc(user);

        // Step 2 — Credit score check
        validateCreditScore(user, request.getLoanType());

        // Step 3 — Active loan limit check
        validateActiveLoanLimit(user);

        // Step 4 — Calculate fees
        BigDecimal interestRate =
                INTEREST_RATES.get(request.getLoanType());

        BigDecimal processingFee = calculateProcessingFee(
                request.getLoanAmount(),
                request.getLoanType());


        // Step 5 — Build and save
        Loan loan = Loan.builder()
                .user(user)
                .loanAmount(request.getLoanAmount())
                .loanType(request.getLoanType())
                .interestRate(interestRate)
                .processingFee(processingFee)
                .tenureMonths(request.getTenureMonths())
                .status(LoanStatus.PENDING)
                .purpose(request.getPurpose())
                .build();

        Loan saved = loanRepository.save(loan);
        log.info("Loan applied: userId={}, type={}, amount={}",
                user.getId(), request.getLoanType(),
                request.getLoanAmount());

        return mapToLoanResponse(saved);
    }


    // ========================
    // Check Eligibility
    // ========================
    @Override
    @Transactional(readOnly = true)
    public LoanEligibilityResponse checkEligibility(LoanRequest request) {
        User user = getCurrentUser();

        // KYC check
        if (user.getPhone() == null
                || user.getDateOfBirth() == null
                || user.getAddresses() == null) {
            return LoanEligibilityResponse.builder()
                    .eligible(false)
                    .message("Complete your profile first — "
                            + "phone, date of birth and "
                            + "address required.")
                    .build();
        }

        // Credit score check
        int minScore = MIN_CREDIT_SCORE
                .get(request.getLoanType());

        if (user.getCreditScore() < minScore) {
            return LoanEligibilityResponse.builder()
                    .eligible(false)
                    .message("Credit score too low. Required: "
                            + minScore + ", Yours: "
                            + user.getCreditScore())
                    .build();
        }

        // Active loans check
        long activeLoans = loanRepository.countByUserIdAndStatus(
                user.getId(), LoanStatus.APPROVED);

        if (activeLoans >= MAX_ACTIVE_LOANS) {
            return LoanEligibilityResponse.builder()
                    .eligible(false)
                    .message("Maximum active loan limit "
                            + "reached (" + MAX_ACTIVE_LOANS
                            + " loans).")
                    .build();
        }

        BigDecimal maxAmount = calculateMaxEligibleAmount(
                user.getCreditScore(), request.getLoanType());

        BigDecimal interestRate =
                INTEREST_RATES.get(request.getLoanType());

        BigDecimal processingFee = calculateProcessingFee(
                request.getLoanAmount(),
                request.getLoanType());

        return LoanEligibilityResponse.builder()
                .eligible(true)
                .message("You are eligible! " +
                        "Max eligible amount based on " +
                        "your credit score: ₹" + maxAmount)
                .interestRate(interestRate)
                .processingFee(processingFee)
                .maxEligibleAmount(maxAmount)
                .build();
    }

    // ========================
    // Get My Loans
    // ========================
    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> getMyLoans() {
        User user = getCurrentUser();
        return loanRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToLoanResponse)
                .toList();
    }

    // ========================
    // Get Loan By ID
    // ========================
    @Override
    @Transactional(readOnly = true)
    public LoanResponse getLoanById(Long id) {
        User user = getCurrentUser();
        Loan loan = findLoanById(id);

        // Customer can only view own loans
        if (user.getRole() == Role.CUSTOMER
                && !loan.getUser().getId().equals(user.getId())) {
            throw new BusinessException(
                    "You are not authorized to view this loan.",
                    HttpStatus.FORBIDDEN);
        }

        return mapToLoanResponse(loan);
    }

    // ========================
    // Submit Collateral
    // (Customer)
    // ========================
    @Override
    @Transactional
    public LoanResponse submitCollateral(Long id, CollateralRequest request) {
        User user = getCurrentUser();
        Loan loan = findLoanById(id);

        // Must be own loan
        if (!loan.getUser().getId().equals(user.getId())) {
            throw new BusinessException(
                    "You are not authorized to update this loan.",
                    HttpStatus.FORBIDDEN);
        }

        // Only allowed when COLLATERAL_REQUIRED
        if (loan.getStatus() != LoanStatus.COLLATERAL_REQUIRED) {
            throw new BusinessException(
                    "Collateral can only be submitted when "
                            + "loan status is COLLATERAL_REQUIRED.",
                    HttpStatus.BAD_REQUEST);
        }

        loan.setCollateralDetails(
                request.getCollateralDetails());

        Loan updatedLoan = loanRepository.save(loan);

        log.info("Collateral submitted: loanId={}, userId={}",
                id, user.getId());

        return mapToLoanResponse(updatedLoan);
    }

    // ========================
    // Cancel Loan (Customer)
    // Only PENDING loans
    // ========================
    @Override
    @Transactional
    public LoanResponse cancelLoan(Long id) {
        User user = getCurrentUser();
        Loan loan = findLoanById(id);

        // Must be own loan
        if (!loan.getUser().getId().equals(user.getId())) {
            throw new BusinessException(
                    "You are not authorized to cancel this loan.",
                    HttpStatus.FORBIDDEN);
        }

        // Only PENDING loans can be canceled
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BusinessException(
                    "Only PENDING loans can be cancelled. "
                            + "Current status: " + loan.getStatus(),
                    HttpStatus.BAD_REQUEST);
        }

        loan.setStatus(LoanStatus.CANCELLED);
        Loan updatedLoan = loanRepository.save(loan);

        log.info("Loan cancelled by customer: loanId={}, userId={}",
                id, user.getId());

        return mapToLoanResponse(updatedLoan);
    }

    // ========================
    // Get All Loans (Manager)
    // with filters
    // ========================
    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> getAllLoans(
            String status, String type,
            LocalDate from, LocalDate to) {

        // Parse optional params
        LoanStatus loanStatus = status != null
                ? parseLoanStatus(status) : null;
        LoanType loanType = type != null
                ? parseLoanType(type) : null;
        LocalDateTime fromDate = from != null
                ? from.atStartOfDay() : null;
        LocalDateTime toDate = to != null
                ? to.atTime(23, 59, 59) : null;

        List<Loan> loans;

        // All 4 filters
        if (loanStatus != null && loanType != null
                && fromDate != null && toDate != null) {
            loans = loanRepository
                    .findByStatusAndLoanTypeAndCreatedAtBetween(loanStatus, loanType, fromDate, toDate);
        }

        // Status + Type
        else if (loanStatus != null && loanType != null) {
            loans = loanRepository
                    .findByStatusAndLoanType(loanStatus, loanType);
        }

        // Status + Date range
        else if (loanStatus != null && fromDate != null && toDate != null) {
            loans = loanRepository
                    .findByStatusAndCreatedAtBetween(loanStatus, fromDate, toDate);
        }

        // Type + Date range
        else if (loanType != null && fromDate != null && toDate != null) {
            loans = loanRepository
                    .findByLoanTypeAndCreatedAtBetween(loanType, fromDate, toDate);
        }

        // Status only
        else if (loanStatus != null) {
            loans = loanRepository.findByStatus(loanStatus);
        }

        // Type only
        else if (loanType != null) {
            loans = loanRepository.findByLoanType(loanType);
        }

        // Date range only
        else if (fromDate != null && toDate != null) {
            loans = loanRepository.findByCreatedAtBetween(fromDate, toDate);
        }

        // No filters — all loans
        else {
            loans = loanRepository.findAll();
        }

        log.info("Fetching loans: status={}, type={}, "
                        + "from={}, to={}, count={}",
                status, type, from, to, loans.size());

        return loans.stream()
                .map(this::mapToLoanResponse)
                .toList();
    }


    // ========================
    // Update Loan Status
    // (Manager)
    // ========================
    @Override
    @Transactional
    public LoanResponse updateLoanStatus(Long id, String status) {
        Loan loan = findLoanById(id);
        LoanStatus newStatus = parseLoanStatus(status);

        validateStatusTransition(loan.getStatus(), newStatus);

        loan.setStatus(newStatus);
        Loan updated = loanRepository.save(loan);

        log.info("Loan status updated: loanId={}, {} -> {}",
                id, loan.getStatus(), newStatus);
        return mapToLoanResponse(updated);
    }

    // ========================
    // Loan Stats (Manager)
    // ========================
    @Override
    @Transactional(readOnly = true)
    public LoanStatsResponse getLoanStats() {
        List<Loan> allLoans = loanRepository.findAll();

        Map<String, Long> countByStatus = allLoans.stream()
                .collect(Collectors.groupingBy(
                        l -> l.getStatus().name(),
                        Collectors.counting()));

        Map<String, Long> countByType = allLoans.stream()
                .collect(Collectors.groupingBy(
                        l -> l.getLoanType().name(),
                        Collectors.counting()));

        Map<String, BigDecimal> amountByType = allLoans.stream()
                .collect(Collectors.groupingBy(
                        l -> l.getLoanType().name(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Loan::getLoanAmount,
                                BigDecimal::add)));

        BigDecimal totalAmount = allLoans.stream()
                .map(Loan::getLoanAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return LoanStatsResponse.builder()
                .totalLoans((long) allLoans.size())
                .totalAmount(totalAmount)
                .countByStatus(countByStatus)
                .countByType(countByType)
                .amountByType(amountByType)
                .pendingLoans(countByStatus
                        .getOrDefault("PENDING", 0L))
                .approvedLoans(countByStatus
                        .getOrDefault("APPROVED", 0L))
                .rejectedLoans(countByStatus
                        .getOrDefault("REJECTED", 0L))
                .underReviewLoans(countByStatus
                        .getOrDefault("UNDER_REVIEW", 0L))
                .build();
    }


    // ========================
    // Helper — Validate KYC
    // ========================
    private void validateKyc(User user) {
        if (user.getPhone() == null
                || user.getDateOfBirth() == null
                || user.getAddresses().isEmpty()) {
            throw new BusinessException(
                    "Complete your profile before applying. "
                            + "Phone, date of birth and address required.",
                    HttpStatus.BAD_REQUEST);
        }
    }


    // ========================
    // Helper — Credit Score
    // ========================
    private void validateCreditScore(
            User user, LoanType loanType) {
        Integer minScore = MIN_CREDIT_SCORE.get(loanType);
        if (user.getCreditScore() < minScore) {
            throw new BusinessException(
                    "Credit score too low for "
                            + loanType.name()
                            + ". Required: " + minScore
                            + ", Your score: "
                            + user.getCreditScore(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    // ========================
    // Helper — Active Loans
    // ========================
    private void validateActiveLoanLimit(User user) {
        long activeLoans = loanRepository
                .countByUserIdAndStatus(
                        user.getId(), LoanStatus.APPROVED);

        if (activeLoans >= MAX_ACTIVE_LOANS) {
            throw new BusinessException(
                    "Maximum active loan limit reached ("
                            + MAX_ACTIVE_LOANS + " loans). "
                            + "Please close existing loans first.",
                    HttpStatus.BAD_REQUEST);
        }

    }

    // ========================
    // Helper — Processing Fee
    // ========================
    private BigDecimal calculateProcessingFee(
            BigDecimal amount, LoanType loanType) {
        BigDecimal feePercent =
                PROCESSING_FEE_PERCENT.get(loanType);
        return amount
                .multiply(feePercent)
                .divide(new BigDecimal("100"), 2,
                        RoundingMode.HALF_UP);
    }

    // ========================
    // Helper — Max Amount
    // ========================
    private BigDecimal calculateMaxEligibleAmount(
            int creditScore, LoanType loanType) {
        BigDecimal baseMax =
                BASE_MAX_AMOUNTS.get(loanType);
        double multiplier;
        if (creditScore >= 800) multiplier = 1.00;
        else if (creditScore >= 750) multiplier = 0.90;
        else if (creditScore > 700) multiplier = 0.75;
        else multiplier = 0.60;

        return baseMax.multiply(BigDecimal.valueOf(multiplier))
                .setScale(2, RoundingMode.HALF_UP);
    }

    // ========================
    // Helper — Status Transition
    // ========================
    private void validateStatusTransition(
            LoanStatus current, LoanStatus next) {
        boolean valid = switch (current) {
            case PENDING -> next == LoanStatus.UNDER_REVIEW
                    || next == LoanStatus.CANCELLED;
            case UNDER_REVIEW -> next == LoanStatus.APPROVED
                    || next == LoanStatus.REJECTED
                    || next == LoanStatus.COLLATERAL_REQUIRED;
            case COLLATERAL_REQUIRED -> next == LoanStatus.APPROVED
                    || next == LoanStatus.COLLATERAL_REQUIRED;
            case APPROVED -> next == LoanStatus.FORECLOSED;
            case REJECTED, FORECLOSED, CANCELLED -> false;
        };

        if (!valid) {
            throw new BusinessException(
                    "Invalid status transition: "
                            + current + " → " + next
                            + ". Valid transitions: "
                            + getValidTransitions(current),
                    HttpStatus.BAD_REQUEST);
        }
    }

    // ========================
    // Helper — Valid Transitions
    // ========================
    private String getValidTransitions(LoanStatus status) {
        return switch (status) {
            case PENDING -> "UNDER_REVIEW";
            case UNDER_REVIEW -> "APPROVED, REJECTED, COLLATERAL_REQUIRED";
            case COLLATERAL_REQUIRED -> "APPROVED, REJECTED";
            case APPROVED -> "FORECLOSED";
            case REJECTED, FORECLOSED, CANCELLED -> "None (terminal state)";
        };
    }


    // ========================
    // Helper — Parse Status
    // ========================
    private LoanStatus parseLoanStatus(String status) {
        try {
            return LoanStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(
                    "Invalid loan status: " + status
                            + ". Valid values: "
                            + Arrays.toString(LoanStatus.values()),
                    HttpStatus.BAD_REQUEST);
        }
    }


    // ========================
    // Helper — Parse LoanType
    // ========================
    private LoanType parseLoanType(String loanType) {
        try {
            return LoanType.valueOf(loanType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(
                    "Invalid Loan type:  " + loanType
                            + ". Valid values: "
                            + Arrays.toString(LoanType.values()),
                    HttpStatus.BAD_REQUEST);
        }
    }

    // ========================
    // Helper — Find Loan By ID
    // ========================
    private Loan findLoanById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan not found with id: " + id));
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
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + email));
    }


    // ========================
    // Helper — Map to Response
    // ========================
    private LoanResponse mapToLoanResponse(Loan loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .loanType(loan.getLoanType())
                .loanAmount(loan.getLoanAmount())
                .interestRate(loan.getInterestRate())
                .processingFee(loan.getProcessingFee())
                .tenureMonths(loan.getTenureMonths())
                .status(loan.getStatus())
                .purpose(loan.getPurpose())
                .collateralDetails(loan.getCollateralDetails())
                .applicantId(loan.getUser().getId())
                .applicantName(loan.getUser().getName())
                .applicantEmail(loan.getUser().getEmail())
                .appliedAt(loan.getCreatedAt())
                .updateAt(loan.getUpdatedAt())
                .build();
    }
}
