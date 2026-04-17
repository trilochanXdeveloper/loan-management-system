package com.loanmanagement.dto.request;

import com.loanmanagement.enums.RejectionReason;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalRequest {

    private RejectionReason rejectionReason;

    @NotBlank(message = "Remarks is required")
    private String remarks;

}
