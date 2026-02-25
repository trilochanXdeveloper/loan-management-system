package com.loanmanagement.repository;

import com.loanmanagement.entity.EmiSchedule;
import com.loanmanagement.enums.EmiStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EmiScheduleRepository extends JpaRepository<EmiSchedule, Long> {
    List<EmiSchedule> findByLoanId(Long loanId);
    List<EmiSchedule> findByStatus(EmiStatus status);
    List<EmiSchedule> findByDueDateBeforeAndStatus(LocalDate date, EmiStatus status);
}