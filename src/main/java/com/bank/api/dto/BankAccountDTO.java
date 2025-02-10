package com.bank.api.dto;

import java.math.BigDecimal;

import com.bank.api.entity.BankAccount;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankAccountDTO
{
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private CustomerDTO customer;

    public BankAccountDTO(BankAccount account)
    {
        this.id = account.getId();
        this.accountNumber = account.getAccountNumber();
        this.balance = account.getBalance();
        this.customer = new CustomerDTO(account.getCustomer());
    }
}
