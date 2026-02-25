package com.loanmanagement.dto.response;

import com.loanmanagement.enums.LoanStatus;
import com.loanmanagement.enums.LoanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanResponse {

    private Long id;
    private Long userId;
    private String userName;
    private BigDecimal loanAmount;
    private LoanType loanType;
    private BigDecimal interestRate;
    private BigDecimal processingFee;
    private Integer tenureMonths;
    private String collateralDetails;
    private LoanStatus status;
    private LocalDateTime createdAt;
}
