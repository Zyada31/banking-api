package com.bank.api.controller;

import com.bank.api.dto.BankResponse;
import com.bank.api.entity.Transaction;
import com.bank.api.service.transaction.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController
{
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * Transfer amounts between any two accounts, including those owned by different customers.
     */
    @PostMapping("/transfer")
    public ResponseEntity<BankResponse<String>> transferMoney(@RequestBody Transaction transferRequest)
    {
        transactionService.transferMoney(transferRequest);
        return ResponseEntity.ok(new BankResponse<>(true, "Transfer successful", "Funds transferred successfully"));
    }

    /**
     * Retrieve transfer history for a given account.
     */
    @GetMapping("/{accountNumber}")
    public ResponseEntity<BankResponse<List<Transaction>>> getTransactions(@PathVariable String accountNumber)
    {
        List<Transaction> transactions = transactionService.getTransactionsByAccount(accountNumber);
        return ResponseEntity.ok(new BankResponse<>(true, "Transaction history retrieved", transactions));
    }
}
