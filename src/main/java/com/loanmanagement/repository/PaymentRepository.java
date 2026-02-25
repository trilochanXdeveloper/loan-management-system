package com.loanmanagement.repository;

import com.loanmanagement.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByEmiScheduleId(Long emiId);
    List<Payment> findByEmiScheduleLoanId(Long loanId);
}