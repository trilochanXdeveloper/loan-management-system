package com.loanmanagement.service;

import com.loanmanagement.dto.request.ApprovalRequest;
import com.loanmanagement.dto.response.ApprovalHistoryResponse;
import com.loanmanagement.dto.response.ApprovalResponse;
import com.loanmanagement.dto.response.LoanResponse;

import java.util.List;

public interface ApprovalService {

    //Manager actions
    LoanResponse moveToUnderReview(Long loanId);
    LoanResponse approveLoan(Long loanId,
                             ApprovalRequest request);
    LoanResponse rejectLoan(Long loanId,
                            ApprovalRequest request);
    LoanResponse requireCollateral(Long loanId,
                                   ApprovalRequest request);

    // View
    List<ApprovalResponse> getAllApprovals();
    ApprovalResponse getApprovalByLoanId(Long loanId);
    List<LoanResponse> getSlaBreachingLoans();
    List<ApprovalHistoryResponse> getLoanHistory(Long loanId);

    //
    List<ApprovalResponse> getMyApprovals();

    // Auto-approval
    boolean tryAutoApprove(Long loanId);
}
