package com.bank.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.bank.api.entity.BankAccount;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BankAccountDTO
{
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private CustomerDTO customer;
    @JsonProperty("isDeleted") // Ensures JSON field is "isDeleted"
    private boolean deleted;

    private LocalDateTime deletedAt; // Soft delete timestamp

    private LocalDateTime createdAt; // New field for account creation timestamp

    public BankAccountDTO(BankAccount account) {
        this.id = account.getId();
        this.accountNumber = account.getAccountNumber();
        this.balance = account.getBalance();
        this.deletedAt = account.getDeletedAt();
        this.deleted = account.isDeleted();
        this.createdAt = account.getCreatedAt(); // Ensure createdAt is mapped
        this.customer = new CustomerDTO(account.getCustomer());
    }
}
