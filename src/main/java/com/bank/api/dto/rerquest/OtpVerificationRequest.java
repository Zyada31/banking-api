package com.bank.api.dto.rerquest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpVerificationRequest
{
    private String transactionId;
    private String otp;
}