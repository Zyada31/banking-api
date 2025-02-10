package com.bank.api.service.bank;

import com.bank.api.entity.BankAccount;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BankAccountService
{
    BankAccount createAccountOrDeposit(String customerName, BigDecimal initialDeposit, String accountNumber);

    BigDecimal getAccountBalance(String accountNumber);

    List<BankAccount> getAccountsByCustomerId(Long customerId);

}
