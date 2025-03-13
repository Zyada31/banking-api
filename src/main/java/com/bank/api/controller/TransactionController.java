package com.bank.api.controller;

import com.bank.api.dto.rerquest.TransactionRequest;
import com.bank.api.dto.response.BankResponse;
import com.bank.api.dto.rerquest.OtpVerificationRequest;
import com.bank.api.dto.response.TransactionDTO;
import com.bank.api.entity.Transaction;
import com.bank.api.exception.BadRequestException;
import com.bank.api.service.otp.OtpService;
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
    private final OtpService otpService;

    public TransactionController(TransactionService transactionService, OtpService otpService)
    {
        this.transactionService = transactionService;
        this.otpService = otpService;
    }

    /**
     * Transfer amounts between any two accounts, including those owned by different customers.
     */
    @PostMapping("/transfer")
    public ResponseEntity<BankResponse<?>> transferMoney(@RequestBody TransactionRequest transferRequest)
    {
        BankResponse<?> transaction = transactionService.transferMoney(transferRequest);
        return ResponseEntity.ok(transaction);
    }

    // Step 2: Verify OTP before processing high-value transactions
    @PostMapping("/transfer/verify")
    public ResponseEntity<?> verifyTransaction(@RequestBody OtpVerificationRequest request)
    {
        // Step 2: Validate OTP (invalid OTP throws error)
        if (!otpService.validateOtp(request.getTransactionId(), request.getOtp())) {
            throw new BadRequestException("Invalid OTP.");
        }

        // Step 3: Process the transaction
        Transaction transaction = transactionService.processTransaction(request.getTransactionId());

        // Step 4: Return transaction details
        return ResponseEntity.ok(new BankResponse<>(true, "Transaction successfully verified", new TransactionDTO(transaction)));
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
