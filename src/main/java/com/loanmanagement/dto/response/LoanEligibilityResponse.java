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
public class LoanEligibilityResponse {

    private boolean eligible;
    private String message;
    private BigDecimal interestRate;
    private BigDecimal processingFee;
    private BigDecimal maxEligibleAmount;

}
