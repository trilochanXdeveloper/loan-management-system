package com.loanmanagement.repository;

import com.loanmanagement.entity.Loan;
import com.loanmanagement.enums.LoanStatus;
import com.loanmanagement.enums.LoanType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUserId(Long userId);
    List<Loan> findByStatus(LoanStatus status);
    List<Loan> findByLoanType(LoanType loanType);
    List<Loan> findByUserIdAndStatus(Long userId, LoanStatus status);
}