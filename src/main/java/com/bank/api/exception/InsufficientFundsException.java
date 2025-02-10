package com.bank.api.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException
{
    private final BigDecimal balance;

    public InsufficientFundsException(String message, BigDecimal balance)
    {
        super(String.format("%s Current balance: %s.", message, balance));
        this.balance = balance;
    }

}
