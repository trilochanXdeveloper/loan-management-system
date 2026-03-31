package com.loanmanagement.enums;

public enum LoanStatus {
    PENDING,
    UNDER_REVIEW,
    COLLATERAL_REQUIRED,
    APPROVED,
    REJECTED,   // Manager rejected
    CANCELLED,  // Customer cancelled
    FORECLOSED
}
 