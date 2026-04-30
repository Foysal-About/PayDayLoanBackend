package com.paydayloan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorporateUserDTO {
    private Long corporateUserId;
    private Long corporateId;
    private String userLoginId;
    private String userName;
    private String email;
    private String mobileNo;
    private String roleCode;
    private String status;
}
