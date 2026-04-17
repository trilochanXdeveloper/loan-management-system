package com.loanmanagement.controller;


import com.loanmanagement.dto.request.ApprovalRequest;
import com.loanmanagement.dto.response.ApprovalHistoryResponse;
import com.loanmanagement.dto.response.ApprovalResponse;
import com.loanmanagement.dto.response.LoanResponse;
import com.loanmanagement.service.ApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
@Tag(name = "Approval Module",
        description = "Loan approval workflow, "
                + "history tracking, SLA monitoring")
public class ApprovalController {

    private final ApprovalService approvalService;

    // ── Manager ───────────────────────────────────────────

    @Operation(summary = "Move loan to UNDER_REVIEW")
    @PostMapping("/{loanId}/under-review")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<LoanResponse> moveToUnderReview(
            @PathVariable Long loanId) {
        return ResponseEntity.ok(
                approvalService.moveToUnderReview(loanId));
    }

    @Operation(summary = "Approve loan",
            description = "Loan must be UNDER_REVIEW "
                    + "or COLLATERAL_REQUIRED")
    @PostMapping("/{loanId}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<LoanResponse> approveLoan(
            @PathVariable Long loanId,
            @RequestBody(required = false) ApprovalRequest request) {
        return ResponseEntity.ok(
                approvalService.approveLoan(loanId, request));
    }


    @Operation(summary = "Reject loan with reason template",
            description = "Loan must be UNDER_REVIEW "
                    + "or COLLATERAL_REQUIRED")
    @PostMapping("/{loanId}/reject")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<LoanResponse> rejectLoan(
            @PathVariable Long loanId,
            @RequestBody ApprovalRequest request) {
        return ResponseEntity.ok(
                approvalService.rejectLoan(loanId, request));
    }

    @Operation(summary = "Require collateral from customer",
            description = "Loan must be UNDER_REVIEW")
    @PostMapping("/{loanId}/collateral-required")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<LoanResponse> requireCollateral(
            @PathVariable Long loanId,
            @RequestBody(required = false) ApprovalRequest request) {
        return ResponseEntity.ok(
                approvalService.requireCollateral(loanId, request));
    }

    @Operation(summary = "Get all approvals — MANAGER only")
    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<ApprovalResponse>> getAllApprovals() {
        return ResponseEntity.ok(
                approvalService.getAllApprovals());
    }

    @Operation(summary = "Get approval by loan ID")
    @GetMapping("/{loanId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApprovalResponse> getApprovalByLoanId(
            @PathVariable Long loanId) {
        return ResponseEntity.ok(
                approvalService.getApprovalByLoanId(loanId));
    }

    @Operation(summary = "Get SLA breaching loans — MANAGER")
    @GetMapping("/pending-sla")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<LoanResponse>> getSlaBreachingLoans() {
        return ResponseEntity.ok(
                approvalService.getSlaBreachingLoans());
    }

    @Operation(summary = "Get full status history of a loan")
    @GetMapping("/history/{loanId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<ApprovalHistoryResponse>> getLoanHistory(
            @PathVariable Long loanId) {
        return ResponseEntity.ok(
                approvalService.getLoanHistory(loanId));
    }

    // ── Customer ──────────────────────────────────────────
    @Operation(summary = "Get my loan approvals — CUSTOMER")
    @GetMapping("/my-approvals")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<ApprovalResponse>> getMyApprovals() {
        return ResponseEntity.ok(approvalService.getMyApprovals());
    }
}
