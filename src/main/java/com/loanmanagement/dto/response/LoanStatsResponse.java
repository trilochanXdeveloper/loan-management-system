package com.loanmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanStatsResponse {

    private Long totalLoans;
    private BigDecimal totalAmount;
    private Map<String, Long> countByStatus;
    private Map<String, Long> countByType;
    private Map<String, BigDecimal> amountByType;
    private Long pendingLoans;
    private Long approvedLoans;
    private Long rejectedLoans;
    private Long underReviewLoans;

}
