package com.loanmanagement.dto.response;

import com.loanmanagement.enums.AddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {

    private Long id;
    private String street;
    private String city;
    private String state;
    private String pincode;
    private AddressType addressType;

}
