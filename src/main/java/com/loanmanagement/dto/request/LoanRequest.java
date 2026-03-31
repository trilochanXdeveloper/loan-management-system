package com.loanmanagement.dto.request;

import com.loanmanagement.enums.LoanType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRequest {

    @NotNull(message = "Loan type is required")
    private LoanType loanType;

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "1000.00",
            message = "Minimum loan amount is ₹.1,000")
    @DecimalMax(value = "10000000.00",
            message = "Maximum loan amount is ₹.1,00,00,000")
    private BigDecimal loanAmount;

    @NotNull(message = "Tenure is required")
    @Min(value = 3, message = "Minimum tenure is 3 months")
    @Max(value = 360, message = "Maximum tenure is 360 months")
    private Integer tenureMonths;

    @NotBlank(message = "Purpose is required")
    @Size(min = 10, max = 500,
            message = "Purpose must be between 10 and 500 characters")
    private String purpose;
}
