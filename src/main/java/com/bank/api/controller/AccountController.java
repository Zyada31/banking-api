package com.bank.api.controller;

import com.bank.api.dto.BankResponse;
import com.bank.api.entity.BankAccount;
import com.bank.api.service.bank.BankAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("accounts")
public class AccountController
{
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    private final BankAccountService bankAccountService;

    @Autowired
    public AccountController(BankAccountService bankAccountService)
    {
        this.bankAccountService = bankAccountService;
    }

    /**
     * Create a new bank account for a customer, with an initial deposit amount.
     * NOTE: A single customer may have multiple bank accounts.
     */
    @PostMapping
    public ResponseEntity<BankResponse<BankAccount>> createOrDeposit(@RequestParam String customerName,
                                                                     @RequestParam BigDecimal initialDeposit,
                                                                     @RequestParam(required = false) String accountNumber)
    {
        BankAccount account = bankAccountService.createAccountOrDeposit(customerName, initialDeposit, accountNumber);
        return ResponseEntity.ok(new BankResponse<>(true, "Account created or deposit successful", account));
    }

    /*
     * Retrieve balances for a given account.
     */
    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<BankResponse<BigDecimal>> getAccountBalance(@PathVariable String accountNumber)
    {
        try
        {
            BigDecimal balance = bankAccountService.getAccountBalance(accountNumber);
            return ResponseEntity.ok(new BankResponse<>(true, "Account balance retrieved successfully", balance));
        }
        catch (RuntimeException e)
        {
            logger.error("Error fetching balance for {}: {}", accountNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new BankResponse<>(false, "Account not found: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankAccount> getAccount(@PathVariable Long id)
    {
        Optional<BankAccount> account = bankAccountService.getAccountById(id);
        return account.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<BankAccount>> getAccountsByCustomer(@PathVariable Long customerId)
    {
        List<BankAccount> accounts = bankAccountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(accounts);
    }

}
