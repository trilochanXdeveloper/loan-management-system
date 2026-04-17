package com.loanmanagement.repository;

import com.loanmanagement.entity.Approval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApprovalRepository
        extends JpaRepository<Approval, Long> {

    Optional<Approval> findByLoanId(Long aLong);

    List<Approval> findByApprovedById(Long managerId);

    boolean existsByLoanId(Long loanId);
}