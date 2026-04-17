package com.loanmanagement.dto.response;

import com.loanmanagement.enums.DecisionType;
import com.loanmanagement.enums.LoanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalResponse {

    private Long id;
    private Long loanId;
    private LoanType loanType;
    private String applicantName;
    private String applicantEmail;
    private DecisionType decision;
    private String remarks;
    private String approvedByName;
    private boolean autoApproved;
    private LocalDateTime slaDeadline;
    private LocalDateTime decidedAt;
}
