package com.bank.api.unit.service.bank;

import com.bank.api.entity.BankAccount;
import com.bank.api.entity.Customer;
import com.bank.api.exception.BadRequestException;
import com.bank.api.repository.BankAccountRepository;
import com.bank.api.repository.CustomerRepository;
import com.bank.api.service.bank.BankAccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BankAccountServiceTest
{
    @InjectMocks
    private BankAccountServiceImpl bankAccountService;

    @Mock
    private BankAccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDepositFailsForZeroAmount_sadPath()
    {
        String customerName = "John Doe";
        BigDecimal zeroDeposit = new BigDecimal("0");

        Exception exception = assertThrows(BadRequestException.class, () -> {
            bankAccountService.createAccountOrDeposit(customerName, zeroDeposit, null);
        });

        assertEquals("Minimum deposit amount is $50", exception.getMessage());
    }

    @Test
    void testCreateNewAccountAndDeposit_happyPath()
    {
        String customerName = "John Doe";
        BigDecimal initialDeposit = new BigDecimal("200");

        Customer savedCustomer = new Customer();
        savedCustomer.setId(1L);
        savedCustomer.setName(customerName);

        when(customerRepository.findByName(customerName)).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
        when(accountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        BankAccount account = bankAccountService.createAccountOrDeposit(customerName, initialDeposit, null);

        assertNotNull(account);
        assertNotNull(account.getCustomer());
        assertEquals(1L, account.getCustomer().getId()); // Ensure the customer ID is properly set
        assertEquals(initialDeposit, account.getBalance());

        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(accountRepository, times(1)).save(any(BankAccount.class));
    }

    @Test
    void testDepositToExistingAccount_happyPath()
    {
        String customerName = "John Doe";
        String accountNumber = "ACC-12345";
        BigDecimal depositAmount = new BigDecimal("150");

        Customer existingCustomer = new Customer();
        existingCustomer.setId(1L);
        existingCustomer.setName(customerName);

        BankAccount existingAccount = new BankAccount();
        existingAccount.setAccountNumber(accountNumber);
        existingAccount.setBalance(new BigDecimal("100"));
        existingAccount.setCustomer(existingCustomer);

        when(customerRepository.findByName(customerName)).thenReturn(Optional.of(existingCustomer));
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BankAccount updatedAccount = bankAccountService.createAccountOrDeposit(customerName, depositAmount, accountNumber);

        assertNotNull(updatedAccount);
        assertEquals(new BigDecimal("250"), updatedAccount.getBalance());
        verify(accountRepository, times(1)).save(existingAccount);
    }

    @Test
    void testDepositForNonexistentAccount_SadPath()
    {
        String customerName = "John Doe";
        String accountNumber = "ACC-99999";
        BigDecimal depositAmount = new BigDecimal("150");

        Customer existingCustomer = new Customer();
        existingCustomer.setName(customerName);

        when(customerRepository.findByName(customerName)).thenReturn(Optional.of(existingCustomer));
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () ->
                bankAccountService.createAccountOrDeposit(customerName, depositAmount, accountNumber)
        );

        assertEquals("Invalid account number format.", exception.getMessage());
    }

    @Test
    void testDepositForWrongCustomerAccount_sadPath()
    {
        String customerName = "John Doe";
        String accountNumber = "ACC-12345";
        BigDecimal depositAmount = new BigDecimal("150");

        Customer existingCustomer = new Customer();
        existingCustomer.setId(1L);
        existingCustomer.setName("Alice Johnson");

        BankAccount existingAccount = new BankAccount();
        existingAccount.setAccountNumber(accountNumber);
        existingAccount.setBalance(new BigDecimal("500"));
        existingAccount.setCustomer(existingCustomer);

        when(customerRepository.findByName(customerName)).thenReturn(Optional.of(new Customer()));
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(existingAccount));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            bankAccountService.createAccountOrDeposit(customerName, depositAmount, accountNumber);
        });

        assertEquals("Specified bank account does not belong to the given customer", exception.getMessage());
    }

    @Test
    void testGetBalanceForNonexistentAccount_SadPath()
    {
        String accountNumber = "ACC-99999";

        Exception exception = assertThrows(RuntimeException.class, () ->
                bankAccountService.getAccountBalance(accountNumber)
        );

        assertEquals("Bank account not found", exception.getMessage());
    }

    @Test
    void testGetBalanceForExistentAccount_happyPath()
    {
        String customerName = "John Doe";
        BigDecimal initialDeposit = new BigDecimal("200");
        String accountNumber = "ACC-12345";

        Customer savedCustomer = new Customer();
        savedCustomer.setId(1L);
        savedCustomer.setName(customerName);

        BankAccount savedAccount = new BankAccount();
        savedAccount.setAccountNumber(accountNumber);
        savedAccount.setCustomer(savedCustomer);
        savedAccount.setBalance(initialDeposit);

        when(customerRepository.findByName(customerName)).thenReturn(Optional.of(savedCustomer));
        when(accountRepository.save(any(BankAccount.class))).thenReturn(savedAccount);
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(savedAccount));

        BankAccount account = bankAccountService.createAccountOrDeposit(customerName, initialDeposit, null);

        assertNotNull(account);

        BigDecimal balance = bankAccountService.getAccountBalance(account.getAccountNumber());

        assertEquals(initialDeposit, balance);
    }

}
