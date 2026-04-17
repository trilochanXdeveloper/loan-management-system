package com.loanmanagement.repository;

import com.loanmanagement.entity.ApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalHistoryRepository
        extends JpaRepository<ApprovalHistory, Long> {

    List<ApprovalHistory> findByLoanIdOrderByChangedAtAsc(
            Long loanId);
}
