package com.loanmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollateralRequest {

    @NotBlank(message = "Collateral details are required")
    @Size(min = 10, max = 1000,
            message = "Collateral details must be between 10 and 1000 characters")
    private String collateralDetails;

}
