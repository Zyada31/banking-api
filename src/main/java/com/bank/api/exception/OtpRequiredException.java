package com.bank.api.exception;

import lombok.Getter;

@Getter
public class OtpRequiredException extends RuntimeException
{
    private final String transactionId;

    public OtpRequiredException(String message, String transactionId)
    {
        super(message);
        this.transactionId = transactionId;
    }
}
