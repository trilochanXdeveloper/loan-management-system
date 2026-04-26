package com.loanmanagement.service.impl;

import com.loanmanagement.dto.request.ApprovalRequest;
import com.loanmanagement.dto.response.ApprovalHistoryResponse;
import com.loanmanagement.dto.response.ApprovalResponse;
import com.loanmanagement.dto.response.LoanResponse;
import com.loanmanagement.entity.Approval;
import com.loanmanagement.entity.ApprovalHistory;
import com.loanmanagement.entity.Loan;
import com.loanmanagement.entity.User;
import com.loanmanagement.enums.DecisionType;
import com.loanmanagement.enums.LoanStatus;
import com.loanmanagement.enums.RejectionReason;
import com.loanmanagement.enums.Role;
import com.loanmanagement.exception.BusinessException;
import com.loanmanagement.exception.ResourceNotFoundException;
import com.loanmanagement.repository.ApprovalHistoryRepository;
import com.loanmanagement.repository.ApprovalRepository;
import com.loanmanagement.repository.LoanRepository;
import com.loanmanagement.repository.UserRepository;
import com.loanmanagement.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final ApprovalHistoryRepository historyRepository;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    private static final double AUTO_APPROVE_MAX_AMOUNT
            = 50000.0;
    private static final int AUTO_APPROVE_MIN_CREDIT
            = 750;
    private static final int SLA_HOURS = 48;

    // ========================
    // Move to Under Review
    // ========================
    @Override
    @Transactional
    public LoanResponse moveToUnderReview(Long loanId) {
        User manager = getCurrentUser();
        Loan loan = findLoanById(loanId);

        // check status
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BusinessException(
                    "Only PENDING loans can be moved to "
                            + "UNDER_REVIEW. Current status: "
                            + loan.getStatus(),
                    HttpStatus.BAD_REQUEST);
        }

        LoanStatus oldStatus = loan.getStatus();
        loan.setStatus(LoanStatus.UNDER_REVIEW);
        loanRepository.save(loan);

        saveHistory(loan, manager, oldStatus,
                LoanStatus.UNDER_REVIEW,
                "Loan moved to under review by manager",
                false);

        log.info("Loan → UNDER_REVIEW: loanId={}, manager={}",
                loanId, manager.getEmail());

        return mapToLoanResponse(loan);
    }


    // ========================
    // Approve Loan
    // ========================
    @Override
    @Transactional
    public LoanResponse approveLoan(Long loanId, ApprovalRequest request) {
        User manager = getCurrentUser();
        Loan loan = findLoanById(loanId);

        validateDecisionStatus(loan,
                "approve",
                LoanStatus.UNDER_REVIEW,
                LoanStatus.COLLATERAL_REQUIRED);

        LoanStatus oldStatus = loan.getStatus();
        loan.setStatus(LoanStatus.APPROVED);
        loanRepository.save(loan);

        String remarks = request != null && request.getRemarks() != null
                ? request.getRemarks()
                : "Loan approved";

        saveOrUpdateApproval(loan, manager,
                DecisionType.APPROVED, remarks, false);

        saveHistory(loan, manager, oldStatus,
                LoanStatus.APPROVED, remarks, false);

        log.info("Loan APPROVED: loanId={}, manager={}",
                loanId, manager.getEmail());

        return mapToLoanResponse(loan);
    }

    // ========================
    // Reject Loan
    // ========================
    @Override
    @Transactional
    public LoanResponse rejectLoan(Long loanId, ApprovalRequest request) {
        User manager = getCurrentUser();
        Loan loan = findLoanById(loanId);

        validateDecisionStatus(loan,
                "reject",
                LoanStatus.UNDER_REVIEW,
                LoanStatus.COLLATERAL_REQUIRED);

        String reason = buildRejectionReason(request);

        LoanStatus oldStatus = loan.getStatus();
        loan.setStatus(LoanStatus.REJECTED);
        loanRepository.save(loan);

        saveOrUpdateApproval(loan, manager,
                DecisionType.REJECTED, reason, false);

        saveHistory(loan, manager, oldStatus,
                LoanStatus.REJECTED, reason, false);

        log.info("Loan REJECTED: loanId={}, reason={}",
                loanId, reason);

        return mapToLoanResponse(loan);
    }

    // ========================
    // Require Collateral
    // ========================
    @Override
    @Transactional
    public LoanResponse requireCollateral(Long loanId, ApprovalRequest request) {
        User manager = getCurrentUser();
        Loan loan = findLoanById(loanId);

        if (loan.getStatus() != LoanStatus.UNDER_REVIEW) {
            throw new BusinessException(
                    "Loan must be UNDER_REVIEW to require "
                            + "collateral. Current status: "
                            + loan.getStatus(),
                    HttpStatus.BAD_REQUEST);
        }

        String remarks = request != null
                && request.getRemarks() != null
                ? request.getRemarks()
                : "Collateral required for loan approval";

        LoanStatus oldStatus = loan.getStatus();
        loan.setStatus(LoanStatus.COLLATERAL_REQUIRED);
        loanRepository.save(loan);

        saveOrUpdateApproval(loan, manager,
                DecisionType.COLLATERAL_REQUIRED,
                remarks, false);

        saveHistory(loan, manager, oldStatus,
                LoanStatus.COLLATERAL_REQUIRED,
                remarks, false);

        log.info("Collateral required: loanId={}, manager={}",
                loanId, manager.getEmail());

        return mapToLoanResponse(loan);
    }

    // ========================
    // Get All Approvals
    // ========================
    @Override
    @Transactional(readOnly = true)
    public List<ApprovalResponse> getAllApprovals() {
        return approvalRepository.findAll()
                .stream()
                .map(this::mapToApprovalResponse)
                .toList();
    }


    // ========================
    // Get Approval By Loan ID
    // ========================
    @Override
    @Transactional(readOnly = true)
    public ApprovalResponse getApprovalByLoanId(Long loanId) {
        Approval approval = approvalRepository
                .findById(loanId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No approval found for "
                                        + "loan id: " + loanId));
        return mapToApprovalResponse(approval);
    }

    // ========================
    // Get SLA Breaching Loans
    // ========================
    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> getSlaBreachingLoans() {
        LocalDateTime deadline = LocalDateTime.now()
                .minusHours(SLA_HOURS);

        return loanRepository
                .findByStatus(LoanStatus.UNDER_REVIEW)
                .stream()
                .filter(loan -> loan.getCreatedAt()
                        .isBefore(deadline))
                .map(this::mapToLoanResponse)
                .toList();
    }

    // ========================
    // Get Loan History
    // ========================
    @Override
    @Transactional(readOnly = true)
    public List<ApprovalHistoryResponse> getLoanHistory(Long loanId) {
        findLoanById(loanId);
        return historyRepository
                .findByLoanIdOrderByChangedAtAsc(loanId)
                .stream()
                .map(this::mapToHistoryResponse)
                .toList();
    }

    // ========================
    // Get My Approvals (Customer)
    // ========================
    @Override
    @Transactional(readOnly = true)
    public List<ApprovalResponse> getMyApprovals() {
        User user = getCurrentUser();
        return loanRepository
                .findByUserId(user.getId())
                .stream()
                .filter(loan -> approvalRepository
                        .existsByLoanId(loan.getId()))
                .map(loan -> approvalRepository
                        .findByLoanId(loan.getId()).get())
                .map(this::mapToApprovalResponse)
                .toList();
    }

    // ========================
    // Try Auto Approve
    // ========================
    @Override
    @Transactional
    public boolean tryAutoApprove(Long loanId) {
        Loan loan = findLoanById(loanId);
        User applicant = loan.getUser();

        boolean eligible = loan.getLoanAmount().doubleValue() <= AUTO_APPROVE_MAX_AMOUNT
                && applicant.getCreditScore() >= AUTO_APPROVE_MIN_CREDIT
                && applicant.getPhone() != null
                && applicant.getDateOfBirth() != null
                && !applicant.getAddresses().isEmpty()
                && loanRepository.countByUserIdAndStatus(
                applicant.getId(), LoanStatus.APPROVED) == 0;

        if (eligible) {
            LoanStatus oldStatus = loan.getStatus();
            loan.setStatus(LoanStatus.APPROVED);
            loanRepository.save(loan);

            User systemManager = userRepository
                    .findFirstByRole(Role.MANAGER)
                    .orElse(applicant);

            String remarks = "Auto-approved: Amount ≤ ₹50,000"
                    + " with credit score ≥ 750";

            saveOrUpdateApproval(loan, systemManager,
                    DecisionType.APPROVED, remarks, true);

            saveHistory(loan, systemManager,
                    oldStatus, LoanStatus.APPROVED,
                    remarks, true);

            log.info("Loan AUTO-APPROVED: loanId={}", loanId);
            return true;
        }
        return false;
    }

    // ── Validate Helpers ──────────────────────────────────
    private void validateDecisionStatus(
            Loan loan, String action,
            LoanStatus... validStatuses) {
        for (LoanStatus valid : validStatuses) {
            if (loan.getStatus() == valid) return;
        }
        throw new BusinessException(
                "Cannot " + action + " loan with status: "
                        + loan.getStatus()
                        + ". Valid statuses: "
                        + java.util.Arrays.toString(validStatuses),
                HttpStatus.BAD_REQUEST);
    }

    // ── Save Helpers ──────────────────────────────────────

    private void saveOrUpdateApproval(
            Loan loan, User manager,
            DecisionType decision,
            String remarks, boolean autoApproved) {
        Approval approval = approvalRepository
                .findByLoanId(loan.getId())
                .orElse(Approval.builder()
                        .loan(loan)
                        .build());

        approval.setApprovedBy(manager);
        approval.setDecision(decision);
        approval.setRemarks(remarks);
        approval.setAutoApproved(autoApproved);
        approval.setSlaDeadline(LocalDateTime.now()
                .plusHours(SLA_HOURS));

        approvalRepository.save(approval);
    }


    private void saveHistory(
            Loan loan, User changedBy,
            LoanStatus fromStaus, LoanStatus toStatus,
            String reason, boolean autoApproved) {

        ApprovalHistory history = ApprovalHistory.builder()
                .loan(loan)
                .changedBy(changedBy)
                .fromStatus(fromStaus)
                .toStatus(toStatus)
                .reason(reason)
                .autoApproved(autoApproved)
                .build();
        historyRepository.save(history);
    }

    // ── Rejection Reason Builder ──────────────────────────

    private String buildRejectionReason(
            ApprovalRequest request) {
        if (request == null) {
            return "Loan rejected by manager";
        }

        if (request.getRejectionReason() != null) {
            String template = switch (
                    request.getRejectionReason()) {
                case INSUFFICIENT_INCOME -> "Rejected: Insufficient income "
                        + "to service the loan";
                case LOW_CREDIT_SCORE -> "Rejected: Credit score below "
                        + "required threshold";
                case INCOMPLETE_DOCUMENTS -> "Rejected: Required documents "
                        + "are incomplete or missing";
                case HIGH_EXISTING_DEBT -> "Rejected: Existing debt "
                        + "obligations are too high";
                case PROPERTY_VALUATION_ISSUE -> "Rejected: Property valuation "
                        + "does not meet requirements";
                case BUSINESS_INSTABILITY -> "Rejected: Business financial "
                        + "stability concerns";
                case AGE_CRITERIA_NOT_MET -> "Rejected: Age criteria not met "
                        + "for this loan type";
                case INVALID_COLLATERAL -> "Rejected: Provided collateral "
                        + "is invalid or insufficient";
                case OTHER -> request.getRemarks() != null
                        ? request.getRemarks()
                        : "Rejected: Other reasons";
            };
            if (request.getRemarks() != null &&
                    request.getRejectionReason() != RejectionReason.OTHER) {
                return template + ". " + request.getRemarks();
            }
            return template;
        }
        return request.getRemarks() != null
                ? request.getRemarks()
                : "Loan rejected by manager";
    }

    // ── Common Helpers ────────────────────────────────────

    // ========================
    // Find Loan By Loan ID
    // ========================
    private Loan findLoanById(Long loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan not found with id: "
                                        + loanId));
    }

    // ========================
    // Get Current User from Security Context
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
    // Map Approval to Approval Response
    // ========================
    private ApprovalResponse mapToApprovalResponse(Approval approval) {
        return ApprovalResponse.builder()
                .id(approval.getId())
                .loanId(approval.getLoan().getId())
                .loanType(approval.getLoan().getLoanType())
                .applicantName(approval.getLoan()
                        .getUser().getName())
                .applicantEmail(approval.getLoan()
                        .getUser().getEmail())
                .decision(approval.getDecision())
                .remarks(approval.getRemarks())
                .approvedByName(approval.getApprovedBy()
                        .getName())
                .autoApproved(approval.isAutoApproved())
                .slaDeadline(approval.getSlaDeadline())
                .decidedAt(approval.getUpdatedAt())
                .build();
    }

    // ========================
    // Map ApprovalHistory to ApprovalHistoryResponse
    // ========================
    private ApprovalHistoryResponse mapToHistoryResponse(
            ApprovalHistory history) {
        return ApprovalHistoryResponse.builder()
                .id(history.getId())
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .reason(history.getReason())
                .changedByName(history.getChangedBy()
                        .getName())
                .autoApproved(history.isAutoApproved())
                .changedAt(history.getChangedAt())
                .build();
    }

    // ========================
    // Map Loan to Loan Response
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
                .collateralDetails(
                        loan.getCollateralDetails())
                .applicantId(loan.getUser().getId())
                .applicantName(loan.getUser().getName())
                .applicantEmail(loan.getUser().getEmail())
                .appliedAt(loan.getCreatedAt())
                .updateAt(loan.getUpdatedAt())
                .build();
    }
}
