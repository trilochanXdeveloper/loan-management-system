package com.loanmanagement.dto.response;

import com.loanmanagement.enums.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalHistoryResponse {
    private Long id;
    private LoanStatus fromStatus;
    private LoanStatus toStatus;
    private String reason;
    private String changedByName;
    private boolean autoApproved;
    private LocalDateTime changedAt;
}
