package com.loanmanagement.repository;

import com.loanmanagement.entity.Loan;
import com.loanmanagement.enums.LoanStatus;
import com.loanmanagement.enums.LoanType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    // ========================
    // Customer — own loans
    // ========================
    List<Loan> findByUserId(Long userId);

    // Manager — filter loans
    List<Loan> findByStatus(LoanStatus status);

    List<Loan> findByLoanType(LoanType loanType);

    List<Loan> findByUserIdAndStatus(Long userId, LoanStatus status);

    List<Loan> findByStatusAndLoanType(LoanStatus status, LoanType loanType);

    // ========================
    // Validation — active
    // loan limit check
    // ========================
    long countByUserIdAndStatus(Long userId, LoanStatus status);

    // Date range filter
    List<Loan> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    List<Loan> findByStatusAndCreatedAtBetween(LoanStatus status, LocalDateTime form, LocalDateTime to);

    List<Loan> findByLoanTypeAndCreatedAtBetween(LoanType type, LocalDateTime from, LocalDateTime to);

    List<Loan> findByStatusAndLoanTypeAndCreatedAtBetween(
            LoanStatus status, LoanType type,
            LocalDateTime from, LocalDateTime to);

}