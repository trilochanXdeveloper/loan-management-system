package com.loanmanagement.controller;

import com.loanmanagement.dto.request.CollateralRequest;
import com.loanmanagement.dto.request.LoanRequest;
import com.loanmanagement.dto.response.LoanEligibilityResponse;
import com.loanmanagement.dto.response.LoanResponse;
import com.loanmanagement.dto.response.LoanStatsResponse;
import com.loanmanagement.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Tag(name = "Loan Management",
        description = "Apply, Check eligibility, " +
                "View, Cancel, Manager operations")
public class LoanController {

    private final LoanService loanService;

    // ── Customer ──────────────────────────────────────────

    // POST /api/loans/apply
    @Operation(summary = "Apply for loan — CUSTOMER only",
            description = "Requires KYC complete + " +
                    "min credit score + max 2 active loans")
    @PostMapping("/apply")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<LoanResponse> applyLoan(
            @Valid @RequestBody LoanRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(loanService.applyLoan(request));
    }

    // POST /api/loans/check-eligibility
    @Operation(summary = "Check loan eligibility before applying")
    @PostMapping("/check-eligibility")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<LoanEligibilityResponse> checkEligibility(
            @Valid @RequestBody LoanRequest request) {
        return ResponseEntity.ok(
                loanService.checkEligibility(request));
    }

    // GET /api/loans/my-loans
    @Operation(summary = "Get my loans — CUSTOMER only")
    @GetMapping("/my-loans")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<LoanResponse>> getMyLoans() {
        return ResponseEntity.ok(
                loanService.getMyLoans());
    }


    // GET /api/loans/{id}
    @Operation(summary = "Get loan by ID — Customer(own) + Manager(any)")
    @GetMapping("/{id}")
    public ResponseEntity<LoanResponse> getLoanById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                loanService.getLoanById(id));
    }

    // PATCH /api/loans/{id}/collateral
    @Operation(summary = "Submit collateral — CUSTOMER only",
            description = "Only when status = COLLATERAL_REQUIRED")
    @PatchMapping("/{id}/collateral")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<LoanResponse> submitCollateral(
            @PathVariable Long id,
            @Valid @RequestBody CollateralRequest request) {
        return ResponseEntity.ok(
                loanService.submitCollateral(id, request));
    }

    //PATCH /api/loans/{id}/cancel
    @Operation(summary = "Cancel loan — CUSTOMER only",
            description = "Only PENDING loans can be cancelled")
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<LoanResponse> cancelLoan(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                loanService.cancelLoan(id));
    }

    // ── Manager ───────────────────────────────────────────

    // GET /api/loans?status=&loanType=&from=&to=
    @Operation(summary = "Get all loans with filters — MANAGER only",
            description = "Filter by status, loanType, date range")
    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<LoanResponse>> getAllLoans(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String loanType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to) {
        return ResponseEntity.ok(
                loanService.getAllLoans(status, loanType, from, to));
    }

    //GET /api/loans/stats
    @Operation(summary = "Get loan statistics — MANAGER only")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<LoanStatsResponse> getLoanStats() {
        return ResponseEntity.ok(loanService.getLoanStats());
    }

    //PATCH /api/loans/{id}/status?status=
    @Operation(summary = "Update loan status — MANAGER only",
            description = "Status transitions: " +
                    "PENDING→UNDER_REVIEW→APPROVED/REJECTED")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<LoanResponse> updateLoanStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(
                loanService.updateLoanStatus(id, status));
    }
}
