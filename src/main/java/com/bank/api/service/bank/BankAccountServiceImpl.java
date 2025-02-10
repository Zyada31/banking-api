package com.bank.api.service.bank;

import com.bank.api.entity.BankAccount;
import com.bank.api.entity.Customer;
import com.bank.api.exception.BadRequestException;
import com.bank.api.exception.ResourceNotFoundException;
import com.bank.api.repository.BankAccountRepository;
import com.bank.api.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BankAccountServiceImpl implements BankAccountService
{
    private static final Logger logger = LoggerFactory.getLogger(BankAccountServiceImpl.class);

    private final BankAccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public BankAccountServiceImpl(BankAccountRepository accountRepository, CustomerRepository customerRepository)
    {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    /*
     * Creates a new customer & account if none exist
     * Deposits into a specific account if provided
     * Defaults to the latest account if multiple exist
     */
    @Override
    @Transactional
    public BankAccount createAccountOrDeposit(String customerName, BigDecimal initialDeposit,
                                              String accountNumber)
    {
        logger.info("Processing deposit for customer: {} with amount: {}, specified account: {}",
                customerName, initialDeposit, accountNumber);

        if (initialDeposit.compareTo(BigDecimal.valueOf(50)) < 0)
        {
            logger.error("Initial deposit {} is below the minimum required ($50)", initialDeposit);
            throw new BadRequestException("Minimum deposit amount is $50");
        }

        Customer customer = customerRepository.findByName(customerName)
                .orElseGet(() -> {
                    logger.info("Customer '{}' not found. Creating new customer.", customerName);
                    Customer newCustomer = new Customer();
                    newCustomer.setName(customerName);
                    return customerRepository.save(newCustomer);
                });

        if (accountNumber != null && !accountNumber.isEmpty())
        {
            Optional<BankAccount> existingAccount = accountRepository.findByAccountNumber(accountNumber);

            if (existingAccount.isPresent())
            {
                if (!existingAccount.get().getCustomer().getId().equals(customer.getId()))
                {
                    logger.error("Account number {} does not belong to customer {}", accountNumber, customerName);
                    throw new BadRequestException("Specified bank account does not belong to the given customer");
                }

                existingAccount.get().setBalance(existingAccount.get().getBalance().add(initialDeposit));
                logger.info("Deposited {} into specified account: {}", initialDeposit, accountNumber);
                return accountRepository.save(existingAccount.get());
            }
            else
            {
                if (!isValidAccountNumber(accountNumber))
                {
                    throw new ResourceNotFoundException("Invalid account number format.");
                }

                BankAccount newAccount = new BankAccount();
                newAccount.setAccountNumber(accountNumber);
                newAccount.setCustomer(customer);
                newAccount.setBalance(initialDeposit);

                logger.info("Creating a new account with number {} for existing customer {}", accountNumber, customer.getId());
                return accountRepository.save(newAccount);
            }
        }

        List<BankAccount> existingAccounts = accountRepository.findByCustomerId(customer.getId());

        if (!existingAccounts.isEmpty())
        {
            BankAccount latestAccount = existingAccounts.get(existingAccounts.size() - 1);
            latestAccount.setBalance(latestAccount.getBalance().add(initialDeposit));
            logger.info("Deposited {} into latest account: {}", initialDeposit, latestAccount.getAccountNumber());
            return accountRepository.save(latestAccount);
        }

        BankAccount newAccount = populateAccount(customer, initialDeposit);
        return accountRepository.save(newAccount);
    }

    @Override
    public List<BankAccount> getAccountsByCustomerId(Long customerId)
    {
        logger.info("Fetching all accounts for customerId: {}", customerId);
        return accountRepository.findByCustomerId(customerId);
    }

    @Override
    public BigDecimal getAccountBalance(String accountNumber)
    {
        logger.info("Fetching balance for account: {}", accountNumber);

        return accountRepository.findByAccountNumber(accountNumber)
                .map(BankAccount::getBalance)
                .orElseThrow(() -> {
                    logger.error("Account number {} not found", accountNumber);
                    return new RuntimeException("Bank account not found");
                });
    }

    private BankAccount populateAccount(Customer customer, BigDecimal initialDeposit)
    {
        BankAccount account = new BankAccount();
        account.setAccountNumber(generateUniqueAccountNumber());
        account.setCustomer(customer);
        account.setBalance(initialDeposit);
        logger.info("Creating a new account '{}' for customer: {}", account.getAccountNumber(), customer.getName());
        return account;
    }

    private String generateUniqueAccountNumber()
    {
        return "ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private boolean isValidAccountNumber(String accountNumber)
    {
        return accountNumber.matches("ACC-[A-Za-z0-9]{8}"); // Example validation: ACC-XXXXXX
    }
}
