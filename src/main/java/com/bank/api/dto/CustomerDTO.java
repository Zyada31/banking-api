package com.bank.api.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.bank.api.entity.BankAccount;
import com.bank.api.entity.Customer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerDTO
{
    private Long id;
    private String name;
    private List<String> accounts;

    public CustomerDTO(Customer customer)
    {
        this.id = customer.getId();
        this.name = customer.getName();
        this.accounts = customer.getAccounts()
                .stream()
                .map(BankAccount::getAccountNumber)
                .collect(Collectors.toList());
    }
}
