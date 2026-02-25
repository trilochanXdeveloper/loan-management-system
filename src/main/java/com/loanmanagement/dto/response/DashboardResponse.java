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
public class DashboardResponse {

    private Long totalApplications;
    private Long approvedLoans;
    private Long rejectedLoans;
    private Long pendingLoans;
    private BigDecimal totalLoanAmount;
    private BigDecimal totalCollections;
    private Long overdueEmis;
}
