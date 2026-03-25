package com.loanmanagement.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCreditScoreRequest {

    @NotNull(message = "Credit score is required")
    @Min(value = 300, message = "Minimum credit score is 300")
    @Max(value = 900, message = "Maximum credit score is 900")
    private Integer creditScore;

    @NotBlank(message = "Reason is required")
    private String reason;

}
