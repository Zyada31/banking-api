package com.bank.api.exception;

public class TransactionExceptions extends RuntimeException
{
    public TransactionExceptions(String message)
    {
        super(message);
    }
}
