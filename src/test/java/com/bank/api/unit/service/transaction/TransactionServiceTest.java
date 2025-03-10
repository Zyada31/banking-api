package com.bank.api.unit.service.transaction;

import com.bank.api.dto.Status;
import com.bank.api.entity.BankAccount;
import com.bank.api.entity.Transaction;
import com.bank.api.exception.TransactionExceptions;
import com.bank.api.repository.BankAccountRepository;
import com.bank.api.repository.TransactionRepository;
import com.bank.api.service.transaction.TransactionLoggerService;
import com.bank.api.service.transaction.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest
{
    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Mock
    private BankAccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionLoggerService transactionLoggerService;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        transactionService = new TransactionServiceImpl(accountRepository, transactionRepository, transactionLoggerService);
    }

    @Test
    void testSuccessfulTransfer_happyPath()
    {
        String fromAccountNumber = "ACC-123";
        String toAccountNumber = "ACC-456";
        BigDecimal transferAmount = new BigDecimal("100");

        BankAccount sender = new BankAccount();
        sender.setAccountNumber(fromAccountNumber);
        sender.setBalance(new BigDecimal("500"));

        BankAccount receiver = new BankAccount();
        receiver.setAccountNumber(toAccountNumber);
        receiver.setBalance(new BigDecimal("200"));

        when(accountRepository.findByAccountNumber(fromAccountNumber)).thenReturn(Optional.of(sender));
        when(accountRepository.findByAccountNumber(toAccountNumber)).thenReturn(Optional.of(receiver));

        Transaction transferRequest = new Transaction();
        transferRequest.setFromAccount(fromAccountNumber);
        transferRequest.setToAccount(toAccountNumber);
        transferRequest.setAmount(transferAmount);

        transactionService.transferMoney(transferRequest);

        assertEquals(new BigDecimal("400"), sender.getBalance());
        assertEquals(new BigDecimal("300"), receiver.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testTransferFailsDueInvalidAmount_sadPath()
    {
        String fromAccountNumber = "ACC-123";
        String toAccountNumber = "ACC-456";
        BigDecimal transferAmount = new BigDecimal("0");

        BankAccount sender = new BankAccount();
        sender.setAccountNumber(fromAccountNumber);
        sender.setBalance(new BigDecimal("500"));

        BankAccount receiver = new BankAccount();
        receiver.setAccountNumber(toAccountNumber);
        receiver.setBalance(new BigDecimal("300"));

        when(accountRepository.findByAccountNumber(fromAccountNumber)).thenReturn(Optional.of(sender));
        when(accountRepository.findByAccountNumber(toAccountNumber)).thenReturn(Optional.of(receiver));

        Transaction transferRequest = new Transaction();
        transferRequest.setFromAccount(fromAccountNumber);
        transferRequest.setToAccount(toAccountNumber);
        transferRequest.setAmount(transferAmount);

        Exception exception = assertThrows(RuntimeException.class, () -> transactionService.transferMoney(transferRequest));

        assertEquals("Transfer amount must be greater than zero.", exception.getMessage());
    }

    @Test
    void testSameAccountTransfer_sadPath()
    {
        String fromAccountNumber = "ACC-123";
        String toAccountNumber = "ACC-123";
        BigDecimal transferAmount = new BigDecimal("200");

        BankAccount sender = new BankAccount();
        sender.setAccountNumber(fromAccountNumber);
        sender.setBalance(new BigDecimal("500"));

        BankAccount receiver = new BankAccount();
        receiver.setAccountNumber(toAccountNumber);
        receiver.setBalance(new BigDecimal("300"));

        when(accountRepository.findByAccountNumber(fromAccountNumber)).thenReturn(Optional.of(sender));
        when(accountRepository.findByAccountNumber(toAccountNumber)).thenReturn(Optional.of(receiver));

        Transaction transferRequest = new Transaction();
        transferRequest.setFromAccount(fromAccountNumber);
        transferRequest.setToAccount(toAccountNumber);
        transferRequest.setAmount(transferAmount);

        Exception exception = assertThrows(RuntimeException.class, () -> transactionService.transferMoney(transferRequest));

        assertEquals("Cannot transfer money to the same account.", exception.getMessage());
    }

    @Test
    void testReceiverAccountNotFoundTransfer_sadPath()
    {
        String fromAccountNumber = "ACC-123";
        String toAccountNumber = "ACC-125";
        BigDecimal transferAmount = new BigDecimal("200");

        BankAccount sender = new BankAccount();
        sender.setAccountNumber(fromAccountNumber);
        sender.setBalance(new BigDecimal("500"));

        when(accountRepository.findByAccountNumber(fromAccountNumber)).thenReturn(Optional.of(sender));

        Transaction transferRequest = new Transaction();
        transferRequest.setFromAccount(fromAccountNumber);
        transferRequest.setToAccount(toAccountNumber);
        transferRequest.setAmount(transferAmount);

        Exception exception = assertThrows(RuntimeException.class, () -> transactionService.transferMoney(transferRequest));

        assertEquals(String.format("Receiver account %s not found.", toAccountNumber), exception.getMessage());
    }

    @Test
    void testSenderAccountNotFoundTransfer_sadPath()
    {
        String fromAccountNumber = "ACC-123";
        String toAccountNumber = "ACC-125";
        BigDecimal transferAmount = new BigDecimal("200");

        BankAccount receiver = new BankAccount();
        receiver.setAccountNumber(toAccountNumber);
        receiver.setBalance(new BigDecimal("300"));

        when(accountRepository.findByAccountNumber(toAccountNumber)).thenReturn(Optional.of(receiver));

        Transaction transferRequest = new Transaction();
        transferRequest.setFromAccount(fromAccountNumber);
        transferRequest.setToAccount(toAccountNumber);
        transferRequest.setAmount(transferAmount);

        Exception exception = assertThrows(RuntimeException.class, () -> transactionService.transferMoney(transferRequest));

        assertEquals("Sender account ACC-123 not found.", exception.getMessage());
    }

    @Test
    void testSenderInsufficientfundsTransfer_sadPath()
    {
        String fromAccountNumber = "ACC-123";
        String toAccountNumber = "ACC-456";
        BigDecimal transferAmount = new BigDecimal("600");

        BankAccount sender = new BankAccount();
        sender.setAccountNumber(fromAccountNumber);
        sender.setBalance(new BigDecimal("500"));

        BankAccount receiver = new BankAccount();
        receiver.setAccountNumber(toAccountNumber);
        receiver.setBalance(new BigDecimal("300"));

        when(accountRepository.findByAccountNumber(fromAccountNumber)).thenReturn(Optional.of(sender));
        when(accountRepository.findByAccountNumber(toAccountNumber)).thenReturn(Optional.of(receiver));

        Transaction transferRequest = new Transaction();
        transferRequest.setFromAccount(fromAccountNumber);
        transferRequest.setToAccount(toAccountNumber);
        transferRequest.setAmount(transferAmount);

        Exception exception = assertThrows(RuntimeException.class, () -> transactionService.transferMoney(transferRequest));

        assertEquals("Insufficient funds in sender's account. Current balance: 500.", exception.getMessage());
    }

    @Test
    void testGetTransferHistory_happyPath()
    {
        String fromAccountNumber = "ACC-123";
        String toAccountNumber = "ACC-456";
        BigDecimal transferAmount = new BigDecimal("100");

        BankAccount sender = new BankAccount();
        sender.setAccountNumber(fromAccountNumber);
        sender.setBalance(new BigDecimal("500"));

        BankAccount receiver = new BankAccount();
        receiver.setAccountNumber(toAccountNumber);
        receiver.setBalance(new BigDecimal("200"));

        when(accountRepository.findByAccountNumber(fromAccountNumber)).thenReturn(Optional.of(sender));
        when(accountRepository.findByAccountNumber(toAccountNumber)).thenReturn(Optional.of(receiver));

        Transaction transferRequest = new Transaction();
        transferRequest.setFromAccount(fromAccountNumber);
        transferRequest.setToAccount(toAccountNumber);
        transferRequest.setAmount(transferAmount);

        transactionService.transferMoney(transferRequest);

        assertEquals(new BigDecimal("400"), sender.getBalance());
        assertEquals(new BigDecimal("300"), receiver.getBalance());

        verify(transactionRepository, times(1)).save(any(Transaction.class));

        List<Transaction> mockTransactionHistory = List.of(
                new Transaction(fromAccountNumber, toAccountNumber, transferAmount, Status.SUCCESS, null, UUID.randomUUID().toString()),
                new Transaction(fromAccountNumber, "ACC-789", new BigDecimal("50"), Status.FAILED, null, UUID.randomUUID().toString()),
                new Transaction("ACC-000", fromAccountNumber, new BigDecimal("30"), Status.SUCCESS, null, UUID.randomUUID().toString())
        );

        when(transactionRepository.findByFromAccountOrToAccount(fromAccountNumber, fromAccountNumber))
                .thenReturn(mockTransactionHistory);

        List<Transaction> transactionList = transactionService.getTransactionsByAccount(fromAccountNumber);

        assertEquals(3, transactionList.size());
    }

    @Test
    void testGetTransactionsFailsForNonexistentAccount()
    {
        String accountNumber = "ACC-123";

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.getTransactionsByAccount(accountNumber);
        });

        assertEquals("Account number does not exist: ACC-123", exception.getMessage());
    }

    @Test
    void testGetTransactionsFailsWhenNoTransactionsFound()
    {
        String accountNumber = "ACC-12345";

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(new BankAccount()));
        when(transactionRepository.findByFromAccountOrToAccount(accountNumber, accountNumber))
                .thenReturn(Collections.emptyList());

        Exception exception = assertThrows(TransactionExceptions.class, () -> {
            transactionService.getTransactionsByAccount(accountNumber);
        });

        assertEquals("No transactions found for account: ACC-12345", exception.getMessage());
    }

}
