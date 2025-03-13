package com.bank.api.service.transaction;

import com.bank.api.dto.rerquest.TransactionRequest;
import com.bank.api.dto.response.BankResponse;
import com.bank.api.dto.Status;
import com.bank.api.dto.response.TransactionDTO;
import com.bank.api.entity.BankAccount;
import com.bank.api.entity.PendingTransaction;
import com.bank.api.entity.Transaction;
import com.bank.api.exception.*;
import com.bank.api.repository.BankAccountRepository;
import com.bank.api.repository.PendingTransactionRepository;
import com.bank.api.repository.TransactionRepository;
import com.bank.api.service.otp.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
public class TransactionServiceImpl implements TransactionService
{
    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final BankAccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionLoggerService transactionLoggerService;
    private final PendingTransactionRepository pendingTransactionRepository;
    private final OtpService otpService;

    @Autowired
    public TransactionServiceImpl(BankAccountRepository accountRepository,
                                  TransactionRepository transactionRepository,
                                  TransactionLoggerService transactionLoggerService,
                                  PendingTransactionRepository pendingTransactionRepository,
                                  OtpService otpService)
    {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionLoggerService = transactionLoggerService;
        this.pendingTransactionRepository = pendingTransactionRepository;
        this.otpService = otpService;
    }

    /**
     * Initiates a money transfer.
     * If the amount is >= $10,000, requires OTP verification.
     */
    @Transactional
    @Override
    public BankResponse<?> transferMoney(TransactionRequest request)
    {
        logger.info("Processing transfer request: From={} To={} Amount={}",
                request.getFromAccount(), request.getToAccount(), request.getAmount());

        validateTransactionRequest(request);

        if (request.getTransactionId() == null || request.getTransactionId().isEmpty())
        {
            request.setTransactionId(UUID.randomUUID().toString());
        }

        try
        {
            if (request.getAmount().compareTo(BigDecimal.valueOf(10000)) >= 0)
            {
                return handleOtpTransaction(request);
            }

            Transaction transaction = executeTransfer(request.getFromAccount(), request.getToAccount(), request.getAmount());

            transactionLoggerService.logTransaction(transaction);

            return new BankResponse<>(true, "Transfer successful", new TransactionDTO(transaction));
        }
        catch (Exception e)
        {
            logger.error("Transfer failed: {} -> {} | Amount: {} | Reason: {}",
                    request.getFromAccount(), request.getToAccount(), request.getAmount(), e.getMessage());

            transactionLoggerService.logFailedTransaction(request, e.getMessage());

            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            throw new RuntimeException("Transaction failed due to: " + e.getMessage(), e);
        }
    }

    /**
     * Fetches transaction history for a specific account.
     */
    @Override
    public List<Transaction> getTransactionsByAccount(String accountNumber)
    {
        logger.info("Fetching transaction history for account: {}", accountNumber);
        List<Transaction> transactions = transactionRepository.findByFromAccountOrToAccount(accountNumber, accountNumber);

        if (transactions.isEmpty())
        {
            logger.warn("No transactions found for account: {}", accountNumber);
            throw new TransactionExceptions("No transactions found for account: " + accountNumber);
        }

        return transactions;
    }

    /**
     * Validates OTP and processes a pending transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public Transaction processTransaction(String transactionId)
    {
        PendingTransaction pendingTransaction = pendingTransactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        try
        {
            Transaction completedTransaction = executeTransfer(
                    pendingTransaction.getFromAccount(),
                    pendingTransaction.getToAccount(),
                    pendingTransaction.getAmount());

            pendingTransactionRepository.delete(pendingTransaction);
            transactionLoggerService.logTransaction(completedTransaction);
            return completedTransaction;
        }
        catch (Exception e)
        {
            logger.error("Transaction processing failed: {} | Reason: {}", transactionId, e.getMessage());

            transactionLoggerService.logFailedTransaction(pendingTransaction, e.getMessage());

            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            throw new RuntimeException("Transaction processing failed due to: " + e.getMessage(), e);
        }
    }

    /**
     * Handles transactions that require OTP.
     */
    private BankResponse<?> handleOtpTransaction(TransactionRequest request)
    {
        pendingTransactionRepository.save(new PendingTransaction(
                request.getTransactionId(),
                request.getFromAccount(),
                request.getToAccount(),
                request.getAmount(),
                LocalDateTime.now()));

//        String otpId = otpService.generateOtp(request.getTransactionId());
        otpService.generateAndSendOtp(request.getTransactionId(), "SE@se.com", "12345678");

        logger.info("OTP required. Transaction ID: {}", request.getTransactionId());

        return new BankResponse<>(true, "OTP required for transaction verification",
                Map.of("transactionId", request.getTransactionId()));
    }

    /**
     * Executes a direct fund transfer.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private Transaction executeTransfer(String fromAccount, String toAccount, BigDecimal amount)
    {
        BankAccount sender = accountRepository.findByAccountNumberForUpdate(fromAccount)
                .orElseThrow(() -> new BadRequestException("Sender account not found"));

        BankAccount receiver = accountRepository.findByAccountNumberForUpdate(toAccount)
                .orElseThrow(() -> new BadRequestException("Receiver account not found"));

        if (sender.getBalance().compareTo(amount) < 0)
        {
            throw new InsufficientFundsException("Insufficient funds.", sender.getBalance());
        }

        // Fetch latest versions to prevent stale reads
        sender = accountRepository.findById(sender.getId()).orElseThrow();
        receiver = accountRepository.findById(receiver.getId()).orElseThrow();

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        accountRepository.save(sender);
        accountRepository.save(receiver);

        Transaction transaction = new Transaction(fromAccount, toAccount, amount, Status.SUCCESS, null, UUID.randomUUID().toString());

        return transactionRepository.save(transaction);
    }

    /**
     * Validates transaction request.
     */
    private void validateTransactionRequest(TransactionRequest request)
    {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new BadRequestException("Transfer amount must be greater than zero.");
        }
        if (request.getFromAccount().equals(request.getToAccount()))
        {
            throw new BadRequestException("Cannot transfer money to the same account.");
        }
        if (!accountRepository.existsByAccountNumber(request.getFromAccount()))
        {
            throw new ResourceNotFoundException("Sender account not found: " + request.getFromAccount());
        }
        if (!accountRepository.existsByAccountNumber(request.getToAccount()))
        {
            throw new ResourceNotFoundException("Receiver account not found: " + request.getToAccount());
        }
    }
}