package com.loanmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryResponse {
    private Long userId;
    private String name;
    private String email;
    private Integer creditScore;

    //Loan stats
    private Long totalLoansApplied;
    private Long activeLoans;
    private Long pendingLoans;
    private Long rejectedLoans;
    private BigDecimal totalAmountBorrowed;

    //KYC status
    private boolean kycComplete;
    private boolean hasPhone;
    private boolean hasDob;
    private boolean hasAddress;
}
