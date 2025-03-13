package com.bank.api.service.transaction;

import com.bank.api.dto.Status;
import com.bank.api.dto.rerquest.TransactionRequest;
import com.bank.api.entity.PendingTransaction;
import com.bank.api.entity.Transaction;
import com.bank.api.exception.ResourceNotFoundException;
import com.bank.api.repository.TransactionRepository;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransactionLoggerService
{
    private static final Logger logger = LoggerFactory.getLogger(TransactionLoggerService.class);

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionLoggerService(TransactionRepository transactionRepository)
    {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Logs a failed transaction for any type of request (Transaction, TransactionRequest, PendingTransaction).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailedTransaction(String fromAccount, String toAccount, BigDecimal amount, String transactionId,
                                     String failureReason)
    {
        try
        {
            Transaction failedTransaction = new Transaction(
                    fromAccount,
                    toAccount,
                    amount,
                    Status.FAILED,
                    failureReason,
                    transactionId != null ? transactionId : UUID.randomUUID().toString()
            );

            transactionRepository.save(failedTransaction);

            logger.error("Failed transaction logged: From={} To={} Amount={} Reason={}",
                    failedTransaction.getFromAccount(), failedTransaction.getToAccount(),
                    failedTransaction.getAmount(), failureReason);
        }
        catch (Exception e)
        {
            logger.error("Error saving failed transaction: {}", e.getMessage());
        }
    }

    /**
     * Logs a failed transaction using a Transaction entity.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailedTransaction(Transaction transaction, String failureReason)
    {
        logFailedTransaction(transaction.getFromAccount(), transaction.getToAccount(), transaction.getAmount(),
                transaction.getTransactionId(), failureReason);
    }

    /**
     * Logs a failed transaction using a TransactionRequest.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailedTransaction(TransactionRequest request, String failureReason)
    {
        logFailedTransaction(request.getFromAccount(), request.getToAccount(), request.getAmount(),
                request.getTransactionId(), failureReason);
    }

    /**
     * Logs a failed transaction using a PendingTransaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailedTransaction(PendingTransaction pendingTransaction, String failureReason)
    {
        logFailedTransaction(pendingTransaction.getFromAccount(), pendingTransaction.getToAccount(),
                pendingTransaction.getAmount(), pendingTransaction.getTransactionId(), failureReason);
    }

    /**
     * Logs a successful transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logTransaction(Transaction transaction)
    {
        try
        {
            Transaction savedTransaction = transactionRepository.findByTransactionId(transaction.getTransactionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transaction.getTransactionId()));

            savedTransaction.setStatus(Status.SUCCESS);
            transactionRepository.save(savedTransaction);

            logger.info("Successful transaction logged: From={} To={} Amount={} TransactionID={}",
                    savedTransaction.getFromAccount(), savedTransaction.getToAccount(), savedTransaction.getAmount(), savedTransaction.getTransactionId());
        }
        catch (OptimisticLockException e)
        {
            logger.error("Optimistic locking failure while saving transaction: {}", e.getMessage());
            throw new RuntimeException("Transaction update failed due to concurrent modification. Try again.");
        }
        catch (Exception e)
        {
            logger.error("Error saving successful transaction: {}", e.getMessage());
        }
    }
}