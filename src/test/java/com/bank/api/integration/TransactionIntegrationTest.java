package com.bank.api.integration;

import com.bank.api.entity.BankAccount;
import com.bank.api.entity.Customer;
import com.bank.api.repository.BankAccountRepository;
import com.bank.api.repository.CustomerRepository;
import com.bank.api.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class TransactionIntegrationTest extends TestContainerConfig
{
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private BankAccountRepository bankAccountRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    private BankAccount senderAccount;
    private BankAccount receiverAccount;

    @BeforeEach
    void setup()
    {
        transactionRepository.deleteAll();
        bankAccountRepository.deleteAll();
        customerRepository.deleteAll();

        Customer sender = new Customer();
        sender.setName("Alice");
        sender = customerRepository.save(sender);

        Customer receiver = new Customer();
        receiver.setName("Bob");
        receiver = customerRepository.save(receiver);

        senderAccount = new BankAccount();
        senderAccount.setAccountNumber("ACC-12345");
        senderAccount.setBalance(new BigDecimal("1000"));
        senderAccount.setCustomer(sender);
        senderAccount = bankAccountRepository.save(senderAccount);

        receiverAccount = new BankAccount();
        receiverAccount.setAccountNumber("ACC-67890");
        receiverAccount.setBalance(new BigDecimal("500"));
        receiverAccount.setCustomer(receiver);
        receiverAccount = bankAccountRepository.save(receiverAccount);
    }

    @Test
    void testSuccessfulMoneyTransfer() throws Exception {
        mockMvc.perform(post("/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "fromAccount": "ACC-12345",
                            "toAccount": "ACC-67890",
                            "amount": 200
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/transactions/{accountNumber}", "ACC-67890")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void testTransferFailsDueToInsufficientFunds() throws Exception
    {
        mockMvc.perform(post("/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "fromAccount": "ACC-12345",
                            "toAccount": "ACC-67890",
                            "amount": 5000
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/transactions/{accountNumber}", "ACC-12345")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)));;
    }


    @Test
    void testTransferFailsToNonexistentAccount() throws Exception
    {
        mockMvc.perform(post("/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "fromAccount": "ACC-12345",
                                "toAccount": "ACC-99999",
                                "amount": 100
                            }
                            """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testTransferFailsWhenSendingToSameAccount() throws Exception
    {
        mockMvc.perform(post("/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "fromAccount": "ACC-12345",
                                "toAccount": "ACC-12345",
                                "amount": 100
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
