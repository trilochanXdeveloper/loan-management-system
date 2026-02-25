package com.loanmanagement.dto.response;

import com.loanmanagement.enums.DecisionType;
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
    private String approvedByName;
    private String approvedByEmail;
    private DecisionType decision;
    private String remarks;
    private LocalDateTime createdAt;
}
