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
    private LoanType loanType;
    private BigDecimal loanAmount;
    private BigDecimal interestRate;
    private BigDecimal processingFee;
    private Integer tenureMonths;
    private LoanStatus status;
    private String purpose;
    private String collateralDetails;

    // Applicant info
    private Long applicantId;
    private String applicantName;
    private String applicantEmail;

    private LocalDateTime appliedAt;
    private LocalDateTime updateAt;

}
