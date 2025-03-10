package com.bank.api.service.transaction;


import com.bank.api.entity.Transaction;
import com.bank.api.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionLoggerService
{
    private static final Logger logger = LoggerFactory.getLogger(TransactionLoggerService.class);

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionLoggerService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailedTransaction(Transaction transaction)
    {
        try
        {
            Transaction failedTransaction = transaction;
            transactionRepository.save(failedTransaction);
            logger.info("Failed transaction logged: {} -> {} | Amount: {} | Reason: {}", transaction);
        }
        catch (Exception e)
        {
            logger.error("Error saving failed transaction: {}", e.getMessage());
        }
    }
}
