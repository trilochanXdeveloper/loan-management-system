package com.loanmanagement.dto.response;

import com.loanmanagement.enums.EmiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmiScheduleResponse {

    private Long id;
    private Long loanId;
    private BigDecimal emiAmount;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private EmiStatus status;
    private LocalDateTime createdAt;
}
