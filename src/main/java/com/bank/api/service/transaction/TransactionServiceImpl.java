package com.bank.api.service.transaction;

import com.bank.api.dto.Status;
import com.bank.api.entity.BankAccount;
import com.bank.api.entity.Transaction;
import com.bank.api.exception.BadRequestException;
import com.bank.api.exception.InsufficientFundsException;
import com.bank.api.exception.ResourceNotFoundException;
import com.bank.api.exception.TransactionExceptions;
import com.bank.api.repository.BankAccountRepository;
import com.bank.api.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService
{
    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final BankAccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionLoggerService transactionLoggerService;

    @Autowired
    public TransactionServiceImpl(BankAccountRepository accountRepository, TransactionRepository transactionRepository,
                                  TransactionLoggerService transactionLoggerService)
    {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionLoggerService = transactionLoggerService;
    }

    @Override
    @Transactional
    public void transferMoney(Transaction transferRequest)
    {
        logger.info("Processing transfer from {} to {} for amount: {}", transferRequest.getFromAccount(),
                transferRequest.getToAccount(), transferRequest.getAmount());

        try {
            if (transferRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new BadRequestException("Transfer amount must be greater than zero.");
            }

            if (transferRequest.getFromAccount().equals(transferRequest.getToAccount()))
            {
                throw new BadRequestException("Cannot transfer money to the same account.");
            }

            BankAccount sender = accountRepository.findByAccountNumber(transferRequest.getFromAccount())
                    .orElseThrow(() -> {
                        String msg = String.format("Sender account %s not found.", transferRequest.getFromAccount());
                        logger.error(msg);
                        return new ResourceNotFoundException(msg);
                    });

            BankAccount receiver = accountRepository.findByAccountNumber(transferRequest.getToAccount())
                    .orElseThrow(() -> {
                        String msg = String.format("Receiver account %s not found.", transferRequest.getToAccount());
                        logger.error(msg);
                        return new ResourceNotFoundException(msg);
                    });

            if (sender.getBalance().compareTo(transferRequest.getAmount()) < 0)
            {
                String message = "Insufficient funds in sender's account.";
                logger.error(message);
                throw new InsufficientFundsException(message, sender.getBalance());
            }

            sender.setBalance(sender.getBalance().subtract(transferRequest.getAmount()));
            receiver.setBalance(receiver.getBalance().add(transferRequest.getAmount()));

            accountRepository.save(sender);
            accountRepository.save(receiver);

            Transaction transaction = new Transaction(
                    sender.getAccountNumber(), receiver.getAccountNumber(),
                    transferRequest.getAmount(), Status.SUCCESS, null
            );

            transactionRepository.save(transaction);

            logger.info("Transfer successful: {} -> {} | Amount: {}", sender.getAccountNumber(),
                    receiver.getAccountNumber(), transferRequest.getAmount());

        }
        catch (Exception e)
        {
            logger.error("Transfer failed: {} -> {} | Amount: {} | Reason: {}", transferRequest.getFromAccount(),
                    transferRequest.getToAccount(), transferRequest.getAmount(), e.getMessage());

            transactionLoggerService.logFailedTransaction(transferRequest.getFromAccount(), transferRequest.getToAccount(),
                    transferRequest.getAmount(), e.getMessage());

            throw e;
        }
    }

    @Override
    public List<Transaction> getTransactionsByAccount(String accountNumber)
    {
        logger.info("Fetching transaction history for account: {}", accountNumber);
        boolean accountExists = accountRepository.findByAccountNumber(accountNumber).isPresent();
        if (!accountExists)
        {
            logger.error("Transaction retrieval failed: Account number {} does not exist.", accountNumber);
            throw new RuntimeException("Account number does not exist: " + accountNumber);
        }

        List<Transaction> transactions = transactionRepository.findByFromAccountOrToAccount(accountNumber, accountNumber);

        if (transactions.isEmpty())
        {
            logger.warn("No transactions found for account: {}", accountNumber);
            throw new TransactionExceptions("No transactions found for account: " + accountNumber);
        }

        return transactions;
    }

}
